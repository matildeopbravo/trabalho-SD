package sd.client;import sd.packets.client.*;import sd.packets.server.*;import sd.server.Reserva;import sd.server.ServerUser;import sd.server.Voo;import sd.server.VooTabelado;import java.io.DataInputStream;import java.io.DataOutputStream;import java.io.IOException;import java.net.Socket;import java.time.LocalDate;import java.util.List;import java.util.Set;public class Client {    private ClientUser client;    private boolean isAdmin;    private final DataOutputStream out;    private final DataInputStream in;    private final Demultiplexer demultiplexer;    public Client(String address, int port) throws IOException {        this.client = null;        this.isAdmin = false; // esta informacao será atualizada depois da resposta do server à autenticacao        Socket socket = new Socket(address, port);        out = new DataOutputStream(socket.getOutputStream());        in = new DataInputStream(socket.getInputStream());        demultiplexer = new Demultiplexer(in);        demultiplexer.start();        addHook(socket);    }    private void addHook(Socket socket) {        Runtime.getRuntime().addShutdownHook(new Thread(() -> {            try {                in.close();                out.close();                socket.close();            } catch (IOException e) {                e.printStackTrace();            }        }));    }    public void setUser(String username, String password) {        client = new ClientUser(username, password);    }    public ServerReply registaUser(String username, String password) {        try {            RegistarPacket packet = new RegistarPacket(username, password);            packet.serialize(out);            return demultiplexer.awaitReplyTo(packet.getId());        } catch (IOException e) {            e.printStackTrace();            return null;        }    }    public ServerReply.Status autenticaUser(String username, String password) {        try {            LoginPacket packet = new LoginPacket(username, password);            packet.serialize(out);            ServerReply r = demultiplexer.awaitReplyTo(packet.getId());            if (r.getStatus() == ServerReply.Status.Success) {                this.client = new ClientUser(username, password);                this.isAdmin = ((TipoUserAutenticadoReply) r).getIsAdmin();                System.out.println("Logged in as " + (isAdmin ? "admin" : "non-admin"));            }            return r.getStatus();        } catch (IOException e) {            return ServerReply.Status.Failure;        }    }    public void efetuaReserva(List<String> cidades, LocalDate dataInicial, LocalDate dataFinal) throws IOException {        ReservaPacket packet = new ReservaPacket(cidades, dataInicial, dataFinal);        packet.serialize(out);    }    public ServerReply.Status cancelaReserva(int idReserva) {        try {            CancelaReservaPacket packet = new CancelaReservaPacket(idReserva);            packet.serialize(out);            return demultiplexer.awaitReplyTo(packet.getId()).getStatus();        } catch (IOException e) {            e.printStackTrace();            return ServerReply.Status.Failure;        }    }    // apenas voos tabelados    public List<VooTabelado> pedeListaVoos() throws IOException {        ListaVoosPacket packet = new ListaVoosPacket();        packet.serialize(out);        ServerReply reply = demultiplexer.awaitReplyTo(packet.getId());        if (reply.getStatus() == ServerReply.Status.Success) {            return ((ListaVoosReply) reply).getVoos();        }        return null;    }    // admin adiciona voo aos tabelados    public boolean adicionaVoo(String origem, String destino, long capacidade) throws IOException {        System.out.println(Voo.lastId);        VooTabelado v = new VooTabelado(origem, destino, capacidade);        System.out.println("Adicionado novo voo " + origem + "->" + destino);        AdicionaVooPacket packet = new AdicionaVooPacket(v);        packet.serialize(out);        ServerReply reply = demultiplexer.awaitReplyTo(packet.getId());        return reply.getStatus() == ServerReply.Status.Success;    }    public boolean isAutenticado() {        return client != null;    }    public boolean isAdmin() {        return isAdmin;    }    public ClientUser getUserAutenticado() {        return client;    }    public ServerReply.Status fazLogout() {        ServerReply.Status status = ServerReply.Status.Failure;        try {            LogOutPacket packet = new LogOutPacket();            packet.serialize(out);            this.client = null;            status = demultiplexer.awaitReplyTo(packet.getId()).getStatus();        } catch (IOException e) {            e.printStackTrace();        }        return status;    }    public Set<Reserva> pedeListaReservas() {        try {            ListaReservasPacket packet = new ListaReservasPacket();            packet.serialize(out);            ServerReply reply = demultiplexer.awaitReplyTo(packet.getId());            if (reply.getStatus() == ServerReply.Status.Success) {                return ((ListaReservasReply) reply).getReservas();            }        } catch (IOException e) {            e.printStackTrace();        }        return null;    }    public List<List<String>> percursosPossiveis(String origem, String destino) {        List<List<String>> l = null;        try {            TodosPercursosPacket packet = new TodosPercursosPacket(origem, destino);            packet.serialize(out);            ServerReply reply = demultiplexer.awaitReplyTo(packet.getId());            if (reply.getStatus() == ServerReply.Status.Success) {                l = ((ListaPercursosReply) reply).getPercursos();            }        } catch (IOException e) {            e.printStackTrace();        }        return l;    }    public List<NotificacaoReply> getNotificacoes() {        return demultiplexer.getNotificacoes();    }    public boolean encerraDia(LocalDate date) {        EncerramentoPacket packet = new EncerramentoPacket(date);        try {            packet.serialize(out);        } catch (IOException e) {            e.printStackTrace();        }        return demultiplexer.awaitReplyTo(packet.getId()).getStatus() == ServerReply.Status.Success;    }    public Set<ServerUser> pedeListaUsers() throws IOException {        ListaUsersPacket packet = new ListaUsersPacket();        packet.serialize(out);        ServerReply reply = demultiplexer.awaitReplyTo(packet.getId());        if (reply.getStatus() == ServerReply.Status.Success) {            return ((ListaUsersReply) reply).getUsers();        }        return null;    }}