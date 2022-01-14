package sd.server;

import sd.OrigemDestino;
import sd.client.ClientUser;
import sd.client.ui.ClientUI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    private ServerSocket serverSocket;
    // username -> info
    private static HashMap<String, ServerUser> users;
    // nao sao todos os voos que existem mas sao os que têm reservas até agora
    // Map<Tabelado, Map<Data,Voo>>
    private static HashMap<LocalDate,Map<VooTabelado,Voo>> voosUsados;
    // conjunto de voos que acontecem diariamente, sobre os quais pode haver uma reserva
    // (origem,destino) -> vooTabelado
    private static HashMap<OrigemDestino,VooTabelado> voosTabelados;
    // idReserva -> Reserva
    private static HashMap<Integer, Reserva> reservas;

    public Server(int port, String address) {
        try {
            this.serverSocket = new ServerSocket(port, 50,InetAddress.getByName(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
        users = new HashMap<>();
        users.put("admin",new ServerUser("admin","password", true));
        users.put("matilde", new ServerUser("matilde","bravo", false));
        voosTabelados = new HashMap<>();
        VooTabelado voo1 = new VooTabelado("Londres", "Amsterdão", 4);
        VooTabelado voo2 = new VooTabelado("Berlim", "Lisboa", 1);
        VooTabelado voo3 = new VooTabelado("Roma", "Lisboa", 2 );
        VooTabelado voo4 = new VooTabelado("Porto", "Veneza", 50);
        VooTabelado voo5 = new VooTabelado("Roma", "Paris", 300 );
        VooTabelado voo6 = new VooTabelado("Paris", "Lisboa", 100 );
        VooTabelado voo7 = new VooTabelado("Porto", "Lisboa", 4);
        VooTabelado voo8 = new VooTabelado("Paris", "Roma", 2 );
        voosTabelados.put(new OrigemDestino(voo1),voo1);
        voosTabelados.put(new OrigemDestino(voo2),voo2);
        voosTabelados.put(new OrigemDestino(voo3),voo3);
        voosTabelados.put(new OrigemDestino(voo4),voo4);
        voosTabelados.put(new OrigemDestino(voo5),voo5);
        voosTabelados.put(new OrigemDestino(voo6),voo6);
        voosTabelados.put(new OrigemDestino(voo7),voo7);
        voosTabelados.put(new OrigemDestino(voo8),voo8);
        reservas = new HashMap<>();

        voosUsados = new HashMap<>();
        addVooUsado(voo1, LocalDate.now());
        addVooUsado(voo2, LocalDate.now().minusDays(2));
    }

    private void addVooUsado(VooTabelado voo, LocalDate date) {
        var value = voosUsados.get(date);
        if(value == null) {
            var m = new HashMap<VooTabelado,Voo>();
            m.put(voo,new Voo(date,voo));
            voosUsados.put(date,m);
        }
        else {
            value.put(voo,new Voo(date,voo));
        }
    }

    public void start() {
        System.out.println("Server listening on "
                + serverSocket.getInetAddress().getHostAddress() +":" + serverSocket.getLocalPort() );
        while(true) {
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
    public static void registaUser(ServerUser usr, DataInputStream in, DataOutputStream out) {
        System.out.println("Recebido pedido de registar user");
        try {
            ClientUser us = ClientUser.deserialize(in);
            System.out.println("User: " + us.getUserName() + " password: "+ us.getPassword());
            if(users.containsKey(us.getUserName())) {
                System.out.println("Uitlizador já existe");
                Reply.Failure.serialize(out);
            }
            else {
                ServerUser su = new ServerUser(us);
                users.put(su.getUserName(),su);
                Reply.Success.serialize(out);
            }
        }
        catch (IOException e) {
            Reply.InvalidFormat.serialize(out);
        }
    }

    public static ServerUser autenticaUser(DataInputStream in, DataOutputStream out) {
        ServerUser serverUser = null;
        try {
            ClientUser clientUser = ClientUser.deserialize(in);
            serverUser = users.get(clientUser.getUserName());
            if(serverUser != null && serverUser.getPassword().equals(clientUser.getPassword())) {
                Reply.Success.serialize(out);
                out.writeBoolean(serverUser.isAdmin());
                serverUser.setIsAuthenticated(true);
                return serverUser;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Reply.Failure.serialize(out);
        return null;
    }
    // todos os percursos para viajar entre uma origem e um destino com no maximo duas escalas (trees voos)
    public static void obtemTodosPercursosPossiveis(DataInputStream in, DataOutputStream out) {
        //String origem = in.readUTF();
        //String destino = in.readUTF();
        // TODO minimazar quantidade de dados transferidos

    }

    public static void efetuaReserva(ServerUser usr, DataInputStream in, DataOutputStream out) {
        try {
            int numCidades = in.readInt();
            var percurso = new ArrayList<>(numCidades);
            for (int i = 0; i < numCidades; i++) {
                System.out.println("Read cidade");
                percurso.add(in.readUTF());
            }
            LocalDate ini = LocalDate.parse(in.readUTF());
            LocalDate fi = LocalDate.parse(in.readUTF());
            Set<VooTabelado> voosPercurso = new HashSet<>();

            Iterator<Object> iter = percurso.iterator();
            String origem = (String) iter.next();
            String destino = null;


            while (iter.hasNext()) {
                if (destino != null) {
                    origem = destino;
                }
                destino = (String) iter.next();
                OrigemDestino o = new OrigemDestino(origem, destino);
                VooTabelado tabelado = voosTabelados.get(o);
                if (tabelado == null) {
                    Reply.Failure.serialize(out);
                    return;
                } else {
                    voosPercurso.add(tabelado);
                }
            }
            LocalDate currentDate = ini;
            boolean allAvailable = true;
            while (!currentDate.isAfter(fi)) {
                allAvailable = true;
                Map<VooTabelado, Voo> todosData = voosUsados.get(currentDate);
                if(todosData != null) {
                    for (VooTabelado v : voosPercurso) {
                        Voo v2 = todosData.get(v);
                        if (v2 != null && v2.getCapacidade() <= 0) {
                            allAvailable = false;
                            // sair do ciclo interior, ir para proxima data
                            break;
                        }
                    }
                }
                if(allAvailable) {
                    break;
                }
                currentDate = currentDate.plusDays(1);
            }
            if(allAvailable) {
                // acrescenta ao map das reservas os que faltam
                Set<Voo> actualVoos = reservaVoos(voosPercurso,currentDate);
                Reserva res = new Reserva(usr.getClientUser(),actualVoos);
                reservas.put(res.getId(),res);
                Reply.Success.serialize(out);
                out.writeInt(res.getId());
            }
            else {
                Reply.Failure.serialize(out);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<Voo> reservaVoos(Set<VooTabelado> percurso, LocalDate currentDate) {
        var todosData =
                voosUsados.computeIfAbsent(currentDate, k -> new HashMap<>());

        for (var tabelado : percurso) {
            Voo v = todosData.get(tabelado);
            if(v == null) {
                v = new Voo(currentDate,tabelado);
                todosData.put(tabelado, v);
            }
            v.diminuiCapacidade();
        }
        return new HashSet<>(todosData.values());
    }

    private static boolean isBetween(LocalDate currentDate, LocalDate ini, LocalDate fi) {
        return currentDate.isAfter(ini) && currentDate.isBefore(fi);
    }

    private static Voo descobreVoo(ArrayList<Object> cidades, LocalDate ini, LocalDate fi) {
        return null;
    }

    public static void mudaOrigem(DataInputStream in, DataOutputStream out) {
        // TODO
    }

    public static void mudaDestino(DataInputStream in, DataOutputStream out) {
        // TODO
    }
    public static void mudaCapacidade(DataInputStream in, DataOutputStream out) {
        // TODO
    }
    public static void encerraDia(ServerUser usr, DataInputStream in, DataOutputStream out) {
        // TODO
    }
    public static void listaVoos(ServerUser usr, DataInputStream in, DataOutputStream out) {
        try {
            Reply.Success.serialize(out);
            out.writeInt(voosTabelados.size());
            for(VooTabelado v : voosTabelados.values()) {
                v.serialize(out);
            }
        }
        catch (IOException e) {
            Reply.Failure.serialize(out);
        }
    }

    public static void cancelaReserva(ServerUser usr , DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        int id = 0;
        try {
            id = dataInputStream.readInt();
            Reserva r = reservas.get(id);
            if(r != null && r.getClientUser().equals(usr.getClientUser())) {
                reservas.remove(id);
                Reply.Success.serialize(dataOutputStream);
            }
            else {
                Reply.Failure.serialize(dataOutputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void adicionaVoo(ServerUser usr , DataInputStream in, DataOutputStream out) {
        try {
            VooTabelado v = VooTabelado.deserialize(in);
            voosTabelados.put(new OrigemDestino(v.getOrigem(),v.getDestino()), v);
            System.out.println("Adicionado novo voo");
            Reply.Success.serialize(out);
        } catch (IOException e) {
            Reply.Failure.serialize(out);
            e.printStackTrace();
        }

    }

    List<List<String>> percursosPossiveis(String origem, String destino) {
        return percursosPossiveis(origem, destino, 3);
    }


    List<List<String>> percursosPossiveis (String origem, String destino, int limiteVoos) {
        List<List<String>> percursos = new ArrayList<>();

        for (VooTabelado voo: voosTabelados.values()) {
            if (voo.getOrigem().equals(origem)) {
                if (voo.getDestino().equals(destino))
                    percursos.add(new ArrayList<>(Arrays.asList(origem, destino)));

                else if (limiteVoos > 1) {
                    List<List<String>> percursosPosteriores =
                            percursosPossiveis(voo.getDestino(), destino, limiteVoos-1);
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

    public static void fazLogout(ServerUser serverUser ,DataInputStream in, DataOutputStream out) {
        if(serverUser != null && serverUser.isAuthenticated()){
            System.out.println("User: " + serverUser.getUserName() + " logged out: ");
            Reply.Success.serialize(out);
        }
        else {
            Reply.Failure.serialize(out);
        }
    }

    public static void mostraReservas(ServerUser serverUser, DataInputStream in, DataOutputStream out) {
        try {
            Reply.Success.serialize(out);
            Set<Reserva> reservasUser = reservas.values()
                    .stream()
                    .filter(r -> r.getClientUser().equals(serverUser.getClientUser()))
                    .collect(Collectors.toSet());
            out.writeInt(reservasUser.size());
            for(Reserva r : reservasUser) {
                r.serialize(out);
            }
        }
        catch (IOException e) {
            Reply.Failure.serialize(out);
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
