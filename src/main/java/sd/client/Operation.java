package sd.client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import sd.server.Server;

public enum Operation {
    Registar(Server::registaUser),
    Autenticar(Server::autenticaUser),
    Reserva(Server::efetuaReserva),
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

    public void callHandleMethod(DataInputStream in,  DataOutputStream out) {
        this.action.accept(in,out);
    }
}
