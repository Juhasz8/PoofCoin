package com.example.poof_ui.Blockchain_Side;

public class TransactionRequest
{
    public double tradeAmount;

    public TransactionRequest(double tradeAmount)
    {
        this.tradeAmount = tradeAmount;
    }

}

class BuyingRequest extends TransactionRequest
{
    public Trader trader;

    public BuyingRequest(Trader trader, double tradeAmount)
    {
        super(tradeAmount);
        this.trader = trader;
    }

}

class SellingRequest extends TransactionRequest implements Comparable<SellingRequest>
{
    public User user;
    public double transactionFee;

    public SellingRequest(User user, double tradeAmount, double transactionFee)
    {
        super(tradeAmount);
        this.user = user;
        this.transactionFee = transactionFee;
    }

    @Override
    public int compareTo(SellingRequest request)
    {

        if (request.transactionFee > this.transactionFee)
        {
            return 1;
        }
        else if (request.transactionFee < this.transactionFee)
        {
            return -1;
        }

        return 0;
    }
}
