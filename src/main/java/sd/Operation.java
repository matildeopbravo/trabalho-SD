package sd;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.BiConsumer;

import sd.server.Server;
import sd.server.ServerUser;

public enum Operation {
    Registar(Server::registaUser),
    Autenticar(null),
    Reserva(Server::efetuaReserva),
    CancelaReserva(Server::cancelaReserva),
    MudaOrigem(Server::mudaOrigem),
    MudaDestino(Server::mudaDestino),
    MudaCapacidade(Server::mudaCapacidade),
    Encerramento(Server::encerraDia),
    ListaVoos(Server::listaVoos);

    private BiConsumer<DataInputStream, DataOutputStream> action;

    Operation(BiConsumer<DataInputStream, DataOutputStream> action) {
        this.action = action;
    }

    public void serialize(DataOutputStream output) throws IOException {
        output.writeInt(this.ordinal());
    }
    public void callHandleMethod(DataInputStream in, DataOutputStream out) {
            this.action.accept(in,out);
    }
    public static ServerUser autenticaUser (DataInputStream in, DataOutputStream out) {
            return Server.autenticaUser(in,out);
    }

    public boolean isAdminOption() {
        return this.equals(Encerramento) || this.equals(MudaCapacidade) || this.equals(MudaDestino)
                || this.equals(MudaOrigem);
    }

}
