package com.example.poof_ui.Blockchain_Side;

import java.util.ArrayList;

public class MerkleTree
{
    public String merkleRoot = "";

    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();

    private void BuildTree()
    {
        ArrayList<String> hashes = new ArrayList<>();
        for(int i = 0; i < transactions.size(); i++)
        {
            hashes.add(Cryptography.ConvertFromTransactionToHash(transactions.get(i)));
        }

        while (hashes.size() > 1)
        {
            ArrayList<String> newHashes = new ArrayList<>();
            for (int i = 0; i < hashes.size(); i += 2)
            {
                String left = hashes.get(i);
                String right = (i + 1 < hashes.size()) ? hashes.get(i + 1) : left;
                newHashes.add(Cryptography.sha256(left + right));
            }
            hashes = newHashes;
        }

        merkleRoot = hashes.get(0);
    }

    public void AddTransaction(Transaction transaction)
    {
        transactions.add(transaction);
        BuildTree();
    }

}
