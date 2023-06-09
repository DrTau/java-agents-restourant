package com.agents.models;

import com.agents.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Order extends Client {
    private static final Logger logger = MyLogger.getLogger();

    private int countOfProcessesInWork;
    private final String visitorName;

    public Order(String clientName, int port, String visitorName) {
        super(clientName, port);

        this.visitorName = visitorName;
    }

    public Order(Socket socket, String clientName, String visitorName) {
        super(socket, clientName);

        this.visitorName = visitorName;
    }

    @Override
    protected void handleMessage(Message message) {
        if (!Objects.equals(message.getDestination(), this.clientName)) {
            return;
        }
        switch (message.getType()) {
            case OrderRequest:
                logger.info("Received order request from " + message.getSource() + " for " + visitorName);
                sendOrderToTheKitchen(message);
                sendOrderToStorage(message);
                break;
            case ProcessRespond:
                processIsDone();
                break;
            default:
                break;
        }
    }

    /**
     * Sends the order to the kitchen
     * 
     * @param message
     *                a message sent by the client
     */
    private void sendOrderToTheKitchen(Message message) {
        try {
            logger.log(Level.INFO, this.clientName + ": Sending order to kitchen");
            Menu menu = Menu.fromJson(message.getData());
            ArrayList<Dish> dishes = menu.getDishes();
            countOfProcessesInWork = dishes.size();

            for (Dish dish : dishes) {
                String processName = dish.getName() + "For" + visitorName;
                processName = processName.replace(" ", "_");
                Process process = new Process(processName, this.socketPort, this.clientName, dish);
                process.startClient();

                Message processAdd = new Message(AgentNames.KITCHEN, this.clientName, MessageType.ProcessRequest,
                        processName);

                sendMessage(processAdd);
                logger.log(Level.INFO, this.clientName + ": Sent process request for " + processName + " to kitchen");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, this.clientName + ": Error sending order to kitchen", e);
        }
    }

    /**
     * Sends the order to the storage
     *
     * @param message the order
     */
    private void sendOrderToStorage(Message message) {
        try {
            logger.log(Level.INFO, this.clientName + ": Sending order to storage");
            Message orderNotification = new Message(AgentNames.STORAGE, this.clientName, MessageType.OrderRequest,
                    message.getData());

            sendMessage(orderNotification);
            logger.log(Level.INFO, this.clientName + ": Sent order request to storage");
        } catch (Exception e) {
            logger.log(Level.SEVERE, this.clientName + ": Error sending order to storage", e);
        }
    }

    /**
     * Decreases the number of processes in work and sends the order to the client
     */
    private void processIsDone() {
        --countOfProcessesInWork;
        logger.log(Level.INFO, this.clientName + ": Received process completion notification, " + countOfProcessesInWork
                + " processes remaining");
        if (countOfProcessesInWork < 1) {
            orderIsReady();
        }
    }

    /**
     * Sends the order to the client
     */
    private void orderIsReady() {
        try {
            logger.log(Level.INFO, this.clientName + ": Order is ready");
            Message orderNotification = new Message(AgentNames.ADMIN, this.clientName, MessageType.OrderRespond,
                    visitorName);

            sendMessage(orderNotification);
            logger.log(Level.INFO, this.clientName + ": Sent order response to admin");
        } catch (Exception e) {
            logger.log(Level.SEVERE, this.clientName + ": Error sending order response to admin", e);
        }
    }
}
