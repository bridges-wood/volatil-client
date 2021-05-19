package com.volatil.client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;

import com.volatil.core.client.Client;
import com.volatil.core.client.Receiver;
import com.volatil.core.client.Transmitter;
import com.volatil.core.utils.Logger;
import com.volatil.core.utils.Message;

public class ChatClient extends Client {
  UserTransmitter outgoing;
  UserReceiver incoming;
  private Logger log;
  private String privatePartner = Message.NON_EXISTENT;
  private boolean awaitingHandshake = false;

  public ChatClient(String[] args) {
    super(args);
    Socket server = getServerSocket();
    outgoing = new UserTransmitter(server, System.console());
    incoming = new UserReceiver(server);
    this.log = log();
  }

  private class UserTransmitter extends Transmitter {
    private PrintWriter out;
    private Reader in;
    private Console console;
    Logger log = new Logger("Transmitter");

    public UserTransmitter(Socket serverSocket, Console console) {
      super(serverSocket);
      this.console = console;
      out = getOutWriter();
      in = console.reader();
    }

    @Override
    public void run() {
      while (!isInterrupted()) {
        try {
          if (!in.ready()) {
            Thread.sleep(getSleepTime());
            continue;
          }
          String input = console.readLine("[Me] ");
          out.println(generateMessage(input));
          if (input.equals("EXIT")) {
            if (!privatePartner.equals(Message.NON_EXISTENT)) {
              privatePartner = Message.NON_EXISTENT;
            } else {
              interrupt();
            }
          }

        } catch (IOException e) {
          log.error(e.getMessage());
        } catch (InterruptedException e) {
          break;
        }
      }
      cleanup();
    }

    @Override
    public void cleanup() {
      try {
        in.close();
      } catch (IOException e) {
        log.error(e.getMessage());
      }
      super.cleanup();
    }

    private String generateMessage(String input) {
      if (input.contains("JOIN")) {
        awaitingHandshake = true;
        String destination = input.split(" ")[1];
        return Message.generatePrivateMessage(input, destination);
      } else if (!privatePartner.equals(Message.NON_EXISTENT)) {
        return Message.generatePrivateMessage(input, privatePartner);
      } else {
        return input;
      }
    }
  }

  private class UserReceiver extends Receiver {
    private BufferedReader in;

    public UserReceiver(Socket server) {
      super(server);
      in = getInReader();
    }

    @Override
    public void run() {
      while (!isInterrupted()) {
        try {
          if (!in.ready()) {
            Thread.sleep(getSleepTime());
            continue;
          }
          String message = in.readLine();
          String partner = Message.extractOrigin(message);
          if (isKillCommand(message)) {
            interrupt();
          } else if (!partner.equals(Message.NON_EXISTENT)) {
            if (awaitingHandshake) {
              awaitingHandshake = false;
              privatePartner = partner;
            }
            System.out.println(message);
          } else {
            System.out.println(message);
          }
        } catch (IOException e) {
          log.error(e.getMessage());
        } catch (InterruptedException e) {
          break;
        }
      }
      cleanup();
    }
  }

  public void start() {
    log.info("Started.");
    outgoing.start();
    incoming.start();

    while (true) {
      if (!outgoing.isAlive() || !incoming.isAlive()) {
        break;
      }
    }
    cleanup();
  }

  @Override
  public void cleanup() {
    outgoing.interrupt();
    incoming.interrupt();
    super.cleanup();
  }

  public static void main(String[] args) {
    ChatClient client = new ChatClient(args);
    client.start();
  }
}
