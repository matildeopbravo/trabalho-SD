package sd;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.BiConsumer;

import sd.server.Server;
import sd.server.ServerUser;

public enum Operation {
    Registar(Server::registaUser),
    Login(null),
    LogOut(Server::fazLogout),
    Reserva(Server::efetuaReserva),
    CancelaReserva(Server::cancelaReserva),
    AdicionaVoo(Server::adicionaVoo),
    Encerramento(Server::encerraDia),
    ListaVoos(Server::listaVoos);

    private BiConsumer<DataInputStream, DataOutputStream> action;

    Operation(BiConsumer<DataInputStream, DataOutputStream> action) {
        this.action = action;
    }


    public void serialize(DataOutputStream output) throws IOException {
        output.writeInt(this.getValue());
    }
    public void callHandleMethod(DataInputStream in, DataOutputStream out) {
            this.action.accept(in,out);
    }
    public static ServerUser autenticaUser (DataInputStream in, DataOutputStream out) {
            return Server.autenticaUser(in,out);
    }

    public boolean isAdminOption() {
        return this.equals(Encerramento)  || this.equals(AdicionaVoo);
                //|| this.equals(MudaCapacidade) || this.equals(MudaDestino)
                //|| this.equals(MudaOrigem);
    }
    public int getValue() {
        return -this.ordinal() -  1;
    }
    public static int getOrdinalFromFake(int fakeValue) {
        return -(fakeValue+1);
    }

    public static Operation getFromFakeOrdinal(int readInt) throws ArrayIndexOutOfBoundsException {
        int real = getOrdinalFromFake(readInt);
        return Operation.values()[real];
    }

}
