package sd;


import sd.packets.client.ClientPacket;
import sd.server.Server;
import sd.server.ServerUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum Operation {
    Registar(Server::registaUser),
    Login(null),
    LogOut(Server::fazLogout),
    Reserva(Server::efetuaReserva),
    CancelaReserva(Server::cancelaReserva),
    AdicionaVoo(Server::adicionaVoo),
    Encerramento(Server::encerraDia),
    ListaVoos(Server::listaVoos),
    ListaReservas(Server::mostraReservas),
    PercursosPossiveis(Server::percursosPossiveis),
    ListaUsers(Server::listaUsers);

    private TriConsumer<ServerUser, ClientPacket, DataOutputStream> action;

    Operation(TriConsumer<ServerUser, ClientPacket, DataOutputStream> action) {
        this.action = action;
    }


    public void serialize(DataOutputStream output) throws IOException {
        output.writeInt(this.getValue());
    }

    public void callHandleMethod(ServerUser susr, ClientPacket clientPacket, DataOutputStream out) {
        this.action.accept(susr, clientPacket, out);
    }

    public static ServerUser autenticaUser(ClientPacket clientPacket, DataOutputStream out) {
        return Server.autenticaUser(clientPacket, out);
    }

    public boolean isAdminOption() {
        return this.equals(Encerramento) || this.equals(AdicionaVoo) || this.equals(ListaUsers);
        //|| this.equals(MudaCapacidade) || this.equals(MudaDestino)
        //|| this.equals(MudaOrigem);
    }

    public int getValue() {
        return -this.ordinal() - 1;
    }

    public static int getOrdinalFromFake(int fakeValue) {
        return -(fakeValue + 1);
    }

    public static Operation getFromFakeOrdinal(int readInt) throws ArrayIndexOutOfBoundsException {
        int real = getOrdinalFromFake(readInt);
        return Operation.values()[real];
    }
}
