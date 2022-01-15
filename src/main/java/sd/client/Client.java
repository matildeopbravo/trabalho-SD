package sd.client;import sd.Operation;import sd.Pair;import sd.packets.client.*;import sd.server.Reply;import sd.server.Reserva;import sd.server.Voo;import sd.server.VooTabelado;import java.io.DataInputStream;import java.io.DataOutputStream;import java.io.IOException;import java.net.Socket;import java.time.LocalDate;import java.util.ArrayList;import java.util.HashSet;import java.util.List;import java.util.Set;public class Client {    private ClientUser user;    private boolean isAdmin;    private DataOutputStream out;    private DataInputStream in;    public Client(String address, int port) throws IOException {        this.user = null;        this.isAdmin = false; // esta informacao será atualizada depois da resposta do server à autenticacao        Socket socket = new Socket(address, port);        out = new DataOutputStream(socket.getOutputStream());        in = new DataInputStream(socket.getInputStream());        addHook(socket);    }    private void addHook(Socket socket) {        Runtime.getRuntime().addShutdownHook(new Thread() {            public void run() {                try {                    in.close();                    out.close();                    socket.close();                } catch (IOException e) {                    e.printStackTrace();                }            }        });    }    public void setUser(String username, String password) {        user = new ClientUser(username, password);    }    public Reply registaUser(String username, String password) {        try {            RegistarPacket packet = new RegistarPacket(username, password);            packet.serialize(out);        } catch (IOException e) {            e.printStackTrace();        }        return Reply.deserialize(in);    }    public Reply autenticaUser(String username, String password) {        try {            LoginPacket packet = new LoginPacket(username, password);            packet.serialize(out);            Reply r = Reply.deserialize(in);            if (r.isSuccess()) {                this.user = new ClientUser(username, password);                this.isAdmin = in.readBoolean();                System.out.println("Logged in as " + (isAdmin ? "admin" : "non-admin"));            }            return r;        } catch (IOException e) {            return Reply.Failure;        }    }    public Pair<Integer, LocalDate> efetuaReserva(List<String> cidades, LocalDate dataInicial, LocalDate dataFinal) throws IOException {        ReservaPacket packet = new ReservaPacket(cidades, dataInicial, dataFinal);        packet.serialize(out);        Reply rep = Reply.deserialize(in);        if (rep.isSuccess()) {            return new Pair<>(in.readInt(), LocalDate.parse(in.readUTF()));        }        return null;    }    public Reply cancelaReserva(int idReserva) {        try {            CancelaReservaPacket packet = new CancelaReservaPacket(idReserva);            packet.serialize(out);        } catch (IOException e) {            e.printStackTrace();        }        return Reply.deserialize(in);    }    // apenas voos tabelados    public List<VooTabelado> pedeListaVoos() throws IOException {        ListaVoosPacket packet = new ListaVoosPacket();        packet.serialize(out);        Reply r = Reply.deserialize(in);        if (r.isSuccess()) {            int size = in.readInt();            if (size >= 0) {                List<VooTabelado> l = new ArrayList<>(size);                for (int i = 0; i < size; i++) {                    l.add(VooTabelado.deserialize(in));                }                return l;            }        }        return null;    }    // admin adiciona voo aos tabelados    public boolean adicionaVoo(String origem, String destino, long capacidade) throws IOException {        System.out.println(Voo.lastId);        VooTabelado v = new VooTabelado(origem, destino, capacidade);        System.out.println("Adicionado novo voo " + origem + "->" + destino);        AdicionaVooPacket packet = new AdicionaVooPacket(v);        packet.serialize(out);        Reply r = Reply.deserialize(in);        return r.equals(Reply.Success);    }    private boolean isSucessful(DataInputStream in) throws IOException {        Reply r = Reply.values()[in.readInt()];        return r.equals(Reply.Success);    }    public boolean isAutenticado() {        return user != null;    }    public boolean isAdmin() {        return isAdmin;    }    public ClientUser getUserAutenticado() {        return user;    }    public Reply fazLogout() {        Reply r = Reply.Failure;        try {            LogOutPacket packet = new LogOutPacket();            packet.serialize(out);            r = Reply.deserialize(in);            if (r.isSuccess()) user = null;        } catch (IOException e) {            e.printStackTrace();        }        return r;    }    public Set<Reserva> pedeListaReservas() {        try {            ListaReservasPacket packet = new ListaReservasPacket();            packet.serialize(out);            Reply rep = Reply.deserialize(in);            if (rep.isSuccess()) {                int size = in.readInt();                var reservas = new HashSet<Reserva>(size);                for (int i = 0; i < size; i++) {                    reservas.add(Reserva.deserialize(in));                }                return reservas;            }        } catch (IOException e) {            e.printStackTrace();        }        return null;    }    public List<List<String>> percursosPossiveis(String origem, String destino) {        List<List<String>> l = null;        try {            TodosPercursosPacket packet = new TodosPercursosPacket(origem, destino);            packet.serialize(out);            Reply r = Reply.deserialize(in);            if (r.isSuccess()) {                int size = in.readInt();                l = new ArrayList<>(size);                for (int i = 0; i < size; i++) {                    int sizePercurso = in.readInt();                    List<String> percurso = new ArrayList<>(sizePercurso);                    l.add(percurso);                    for (int j = 0; j < sizePercurso; j++) {                        percurso.add(in.readUTF());                    }                }            }        } catch (IOException e) {            e.printStackTrace();        }        return l;    }    public void encerraDia(LocalDate date) {        // TODO    }}