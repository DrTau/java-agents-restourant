package com.agents;

public enum MessageType {
    Ping,
    MenuRequest, // request for menu (from customer to admin and from admin to storage)
    MenuRespond, // response for menu (from storage to admin and from admin to customer)
    OrderAdd,
    OrderRequest,
    OrderRespond,
    ProcessRequest,
    ProcessRespond,
    WorkRequest,
    WorkRespond,
    DishRequestRespond,
    InstrumentsRequest,
    InstrumentsRespond,
    ProductRequest,
    ProductRespond
}
