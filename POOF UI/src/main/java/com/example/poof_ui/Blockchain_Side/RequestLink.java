package com.example.poof_ui.Blockchain_Side;

public class RequestLink
{
    public RequestLink next;

    //the amount of selling and buying requests that happened in this update cycle
    public int cycleSellingRequestAmount;
    public int cycleBuyingRequestAmount;

    public RequestLink previous;
}
