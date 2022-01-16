package sd.server;

import sd.Operation;
import sd.OrigemDestino;
import sd.client.ClientUser;
import sd.client.ui.ClientUI;
import sd.exceptions.UnexpectedPacketTypeException;
import sd.packets.client.*;
import sd.packets.server.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Server {
    private ServerSocket serverSocket;
    // username -> info
    private static DashMap<String, ServerUser> users;
    // nao sao todos os voos que existem mas sao os que têm reservas até agora
    // Map<Tabelado, Map<Data,Voo>>
    private static DashMap<LocalDate, DashMap<VooTabelado, Voo>> voosUsados;
    // conjunto de voos que acontecem diariamente, sobre os quais pode haver uma reserva
    // (origem,destino) -> vooTabelado
    private static DashMap<OrigemDestino, VooTabelado> voosTabelados;
    // idReserva -> Reserva
    private static DashMap<Integer, Reserva> reservas;

    private static Set<LocalDate> diasEncerrados;
    private static ReadWriteLock diasEncerradosLock;

    public Server(int port, String address) {
        try {
            this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
        users = new DashMap<>();
        users.put("admin", new ServerUser("admin", "password", true));
        users.put("matilde", new ServerUser("matilde", "bravo", false));
        voosTabelados = new DashMap<>();
        VooTabelado voo1 = new VooTabelado("Londres", "Amsterdão", 4);
        VooTabelado voo2 = new VooTabelado("Berlim", "Lisboa", 1);
        VooTabelado voo3 = new VooTabelado("Roma", "Lisboa", 2);
        VooTabelado voo4 = new VooTabelado("Porto", "Veneza", 50);
        VooTabelado voo5 = new VooTabelado("Roma", "Paris", 300);
        VooTabelado voo6 = new VooTabelado("Paris", "Lisboa", 100);
        VooTabelado voo7 = new VooTabelado("Porto", "Lisboa", 4);
        VooTabelado voo8 = new VooTabelado("Paris", "Roma", 2);
        voosTabelados.put(new OrigemDestino(voo1), voo1);
        voosTabelados.put(new OrigemDestino(voo2), voo2);
        voosTabelados.put(new OrigemDestino(voo3), voo3);
        voosTabelados.put(new OrigemDestino(voo4), voo4);
        voosTabelados.put(new OrigemDestino(voo5), voo5);
        voosTabelados.put(new OrigemDestino(voo6), voo6);
        voosTabelados.put(new OrigemDestino(voo7), voo7);
        voosTabelados.put(new OrigemDestino(voo8), voo8);
        reservas = new DashMap<>();

        voosUsados = new DashMap<>();
        addVooUsado(voo1, LocalDate.now());
        addVooUsado(voo2, LocalDate.now().minusDays(2));
        diasEncerrados = new HashSet<>();
        diasEncerradosLock = new ReentrantReadWriteLock();
    }

    private void addVooUsado(VooTabelado voo, LocalDate date) {
        var value = voosUsados.computeIfAbsent(date, k -> new DashMap<>());
        value.put(voo, new Voo(date, voo));
    }

    public void start() {
        System.out.println("Server listening on "
                + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        while (true) {
            try {
                Socket s = serverSocket.accept();
                System.out.println("Client "
                        + ClientUI.ANSI_BLUE + s.getInetAddress().getHostAddress() + ClientUI.ANSI_RESET + " connected");
                new Thread(new Worker(s)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void registaUser(ServerUser usr, ClientPacket clientPacket, DataOutputStream out) {
        System.out.println("Recebido pedido de registar user");
        try {
            try {
                if (!clientPacket.getType().equals(Operation.Registar))
                    throw new UnexpectedPacketTypeException();
                RegistarPacket registarPacket = (RegistarPacket) clientPacket;
                ClientUser us = new ClientUser(registarPacket.getUsername(), registarPacket.getPassword());
                System.out.println("User: " + us.getUserName() + " password: " + us.getPassword());
                ServerUser su = new ServerUser(us);
                ServerUser registedUser = users.putIfAbsent(su.getUserName(),su);

                ServerReply.Status status =  ServerReply.Status.Success;
                if(registedUser != null){
                    System.out.println("Uitlizador já existe");
                    status =  ServerReply.Status.Failure;
                }
                StatusReply reply = new StatusReply(clientPacket.getId(), status);
                reply.serialize(out);
            } catch (UnexpectedPacketTypeException e) {
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.InvalidFormat);
                reply.serialize(out);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ServerUser autenticaUser(ClientPacket clientPacket, DataOutputStream out) {
        ServerUser serverUser = null;
        try {
            if (!clientPacket.getType().equals(Operation.Login))
                throw new UnexpectedPacketTypeException();
            LoginPacket loginPacket = (LoginPacket) clientPacket;
            ClientUser clientUser = new ClientUser(loginPacket.getUsername(), loginPacket.getPassword());

            serverUser = users.get(clientUser.getUserName());
            if(serverUser == null || serverUser.isAuthenticated()) {
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Failure);
                reply.serialize(out);
                return null;
            }
            if (serverUser.getPassword().equals(clientUser.getPassword())) {
                TipoUserAutenticadoReply reply = new TipoUserAutenticadoReply(clientPacket.getId(), ServerReply.Status.Success,
                        serverUser.isAdmin());
                reply.serialize(out);
                serverUser.setIsAuthenticated(true);
                return serverUser;
            }
        } catch (IOException | UnexpectedPacketTypeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void efetuaReserva(ServerUser usr, ClientPacket clientPacket, DataOutputStream out) {
        try {
            if (!clientPacket.getType().equals(Operation.Reserva))
                throw new UnexpectedPacketTypeException();
            ReservaPacket reservaPacket = (ReservaPacket) clientPacket;
            List<String> percurso = reservaPacket.getCidades();
            System.out.println("Read these cities: " + percurso.toString());
            LocalDate ini = reservaPacket.getDataInicial();
            LocalDate fi = reservaPacket.getDataFinal();
            List<VooTabelado> voosPercurso = new ArrayList<>();

            Iterator<String> iter = percurso.iterator();
            String origem = iter.next();
            String destino = null;


            while (iter.hasNext()) {
                if (destino != null) {
                    origem = destino;
                }
                destino = iter.next();
                OrigemDestino o = new OrigemDestino(origem, destino);
                VooTabelado tabelado = voosTabelados.get(o);
                if (tabelado == null) {
                    usr.addNotification("Não existem voos que correspondam ao seu percurso.");
                    return;
                } else {
                    voosPercurso.add(tabelado);
                }
            }
            LocalDate currentDate ;
            diasEncerradosLock.readLock().lock();
            boolean allAvailable = false;
            var lockedVoos = new HashSet<Voo>();
            DashMap<VooTabelado,Voo> todosData = null;
            for (currentDate = ini ; !currentDate.isAfter(fi) ; currentDate = currentDate.plusDays(1)) {
                if(!diasEncerrados.contains(currentDate)) {
                    allAvailable = true;
                    lockedVoos.clear();
                    todosData = voosUsados.computeIfAbsent(currentDate, x -> new DashMap<>());
                    todosData.lock();
                    for (VooTabelado v : voosPercurso.stream().sorted().collect(Collectors.toList())) {
                        Voo v2 = todosData.get(v);
                        if (v2 != null) {
                            v2.lock();
                            if (v2.getCapacidade() <= 0) {
                                allAvailable = false;
                                v2.unlock();
                                break;
                            } else {
                                lockedVoos.add(v2);
                            }
                        }
                    }
                    if (allAvailable) {
                        break;
                    }
                    else {
                        todosData.unlock();
                        lockedVoos.forEach(Voo::unlock);
                    }
                }
            }

            if (allAvailable) {
                var actualVoos =  new HashSet<Voo>();

                for (var tabelado : voosPercurso) {
                    Voo v = todosData.get(tabelado);
                    if (v == null) {
                        v = new Voo(currentDate, tabelado);
                        todosData.put(tabelado, v);
                    }
                    v.diminuiCapacidade();
                    actualVoos.add(v);
                }
                lockedVoos.forEach(Voo::unlock);
                todosData.unlock();
                Reserva res = new Reserva(usr.getClientUser(), actualVoos);
                reservas.put(res.getId(), res);
                usr.addNotification("Os seus voos foram reservados com sucesso (ID da reserva " + res.getId() + ").");
            }
            else {
                usr.addNotification("Não foi possível reservar os voos para os dias indicados.");
            }
            diasEncerradosLock.readLock().unlock();

        } catch (UnexpectedPacketTypeException e) {
            e.printStackTrace();
        }
    }

    private static boolean isBetween(LocalDate currentDate, LocalDate ini, LocalDate fi) {
        return currentDate.isAfter(ini) && currentDate.isBefore(fi);
    }

    public static void encerraDia(ServerUser usr, ClientPacket clientPacket, DataOutputStream out) {
        try {
            if (!clientPacket.getType().equals(Operation.Encerramento)) {
                throw new UnexpectedPacketTypeException();
            }
            EncerramentoPacket packet = (EncerramentoPacket) clientPacket;
            LocalDate data = packet.getDate();

            boolean diaJaEstavaEncerrado = true;
            diasEncerradosLock.writeLock().lock();
            try {
                if (!diasEncerrados.contains(data)) {
                    diaJaEstavaEncerrado = false;
                    diasEncerrados.add(data);
                }
            }
            finally {
                diasEncerradosLock.writeLock().unlock();
            }

            if (!diaJaEstavaEncerrado) {
                for (Reserva reserva : reservas.values(Reserva::clone)) {
                    if (reserva.getVoos().stream().anyMatch(v -> v.getData().equals(data))) {
                        reservas.remove(reserva.getId());
                        ServerUser user = users.get(reserva.getClientUser().getUserName());
                        user.addNotification("Devido ao encerramento dos voos do dia " + data
                                + ", a sua reserva (ID " + reserva.getId() + ") foi cancelada.");
                    }
                }
                voosUsados.remove(data);
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Success);
                reply.serialize(out);
            }
            else {
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Failure);
                reply.serialize(out);
            }
        }
        catch (IOException | UnexpectedPacketTypeException e) {
            e.printStackTrace();
        }
    }
    public static void listaVoos(ServerUser usr, ClientPacket clientPacket, DataOutputStream out) {
        try {
            List<VooTabelado> voos = (List<VooTabelado>) voosTabelados.values( v -> v);
            ListaVoosReply reply =
                    new ListaVoosReply(clientPacket.getId(), ServerReply.Status.Success, voos);
            reply.serialize(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cancelaReserva(ServerUser usr, ClientPacket clientPacket, DataOutputStream out) {
        try {
            if (!clientPacket.getType().equals(Operation.CancelaReserva))
                throw new UnexpectedPacketTypeException();
            CancelaReservaPacket cancelaReservaPacket = (CancelaReservaPacket) clientPacket;
            int id = cancelaReservaPacket.getIdReserva();
            Reserva r = reservas.remove(id);
            StatusReply reply;
            if (r != null && r.getClientUser().equals(usr.getClientUser())) {
                reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Success);
            } else {
                reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Failure);
            }
            reply.serialize(out);
        } catch (UnexpectedPacketTypeException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void adicionaVoo(ServerUser usr, ClientPacket clientPacket, DataOutputStream out) {
        try {
            try {
                if (!clientPacket.getType().equals(Operation.AdicionaVoo))
                    throw new UnexpectedPacketTypeException();
                AdicionaVooPacket adicionaVooPacket = (AdicionaVooPacket) clientPacket;
                VooTabelado v = adicionaVooPacket.getVooTabelado();
                voosTabelados.put(new OrigemDestino(v.getOrigem(), v.getDestino()), v);
                System.out.println("Adicionado novo voo");
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Success);
                reply.serialize(out);
            } catch (UnexpectedPacketTypeException e) {
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Failure);
                reply.serialize(out);
                e.printStackTrace();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void percursosPossiveis(ServerUser serverUser, ClientPacket clientPacket,
                                          DataOutputStream out) {

        try {
            if (!clientPacket.getType().equals(Operation.PercursosPossiveis))
                throw new UnexpectedPacketTypeException();
            TodosPercursosPacket todosPercursosPacket = (TodosPercursosPacket) clientPacket;
            var percursos = percursosPossiveis(todosPercursosPacket.getOrigem(),
                    todosPercursosPacket.getDestino());
            if (percursos.isEmpty()) {
                ListaPercursosReply reply =
                        new ListaPercursosReply(clientPacket.getId(), ServerReply.Status.Failure, null);
                reply.serialize(out);
            } else {
                ListaPercursosReply reply =
                        new ListaPercursosReply(clientPacket.getId(), ServerReply.Status.Success, percursos);
                reply.serialize(out);
            }
        } catch (IOException | UnexpectedPacketTypeException e) {
            e.printStackTrace();
        }

    }

    private static List<List<String>> percursosPossiveis(String origem, String destino) {
        return percursosPossiveis(origem, destino, 3);
    }

    private static List<List<String>> percursosPossiveis(String origem, String destino, int limiteVoos) {
        List<List<String>> percursos = new ArrayList<>();

        for (VooTabelado voo : voosTabelados.values(v->v)) {
            if (voo.getOrigem().equals(origem)) {
                if (voo.getDestino().equals(destino))
                    percursos.add(new ArrayList<>(Arrays.asList(origem, destino)));

                else if (limiteVoos > 1) {
                    List<List<String>> percursosPosteriores =
                            percursosPossiveis(voo.getDestino(), destino, limiteVoos - 1);
                    for (List<String> percursoPosterior : percursosPosteriores) {
                        List<String> novoPercurso = new ArrayList<>(List.of(origem));
                        novoPercurso.addAll(percursoPosterior);
                        percursos.add(novoPercurso);
                    }
                }
            }
        }

        return percursos;
    }

    public static void fazLogout(ServerUser serverUser, ClientPacket clientPacket, DataOutputStream out) {
        try {
            if (serverUser != null && serverUser.isAuthenticated()) {
                serverUser.setIsAuthenticated(false);
                System.out.println("User: " + serverUser.getUserName() + " logged out: ");
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Success);
                reply.serialize(out);
            } else {
                StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Failure);
                reply.serialize(out);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mostraReservas(ServerUser serverUser, ClientPacket clientPacket, DataOutputStream out) {
        try {
            Set<Reserva> reservasUser = reservas.values(Reserva::clone)
                    .stream()
                    .filter(r -> r.getClientUser().equals(serverUser.getClientUser()))
                    .collect(Collectors.toSet());
            ListaReservasReply reply =
                    new ListaReservasReply(clientPacket.getId(), ServerReply.Status.Success, reservasUser);
            reply.serialize(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listaUsers(ServerUser serverUser, ClientPacket clientPacket, DataOutputStream outputStream) {
        ListaUsersReply reply =
                new ListaUsersReply(clientPacket.getId(), ServerReply.Status.Success, (Set<ServerUser>) users.values(v -> v));
        try {
            reply.serialize(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //private static Voo getVooDisponivel(VooTabelado tabelado, LocalDate ini, LocalDate fi) {
    //    // se nao houver  voo disponivel entre essas data devolve null
    //    // adiciona à lista de voosReserva se for o selecionado e nao estiver ja
    //    Voo v = null;
    //    Map<LocalDate,Voo> todos = voosComReserva.get(tabelado);
    //    LocalDate currDate = ini;
    //    while(currDate.isBefore(fi.plusDays(1))) {
    //        v = todos.get(currDate);
    //        if( v != null) {
    //            if(v.getCapacidade() > 0) return v;
    //            else {
    //                currDate = currDate.plusDays(1);
    //            }
    //        }
    //        else {
    //            v = new Voo(currDate, tabelado);
    //            todos.put(currDate,v);
    //            return v;
    //        }
    //    }
    //    return v;
    //}
}
