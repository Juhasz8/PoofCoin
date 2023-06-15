package com.example.poof_ui.Blockchain_Side;

import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    public long timeStamp;

    //private byte[] blockData;
    public MerkleTree dataTree;

    //just for debugging and printing purposes
    public boolean isTrusted = false;

    public Block(byte[] blockData, String previousHash)
    {
        //this.blockData = blockData;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        dataTree = new MerkleTree();
    }

    public void BlockMined(String hash)
    {
        this.hash = hash;
        //notify some other whatever
    }

    /*
    public void AddData(byte[] dataToAdd)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try
        {
            if(blockData != null)
                outputStream.write(blockData);
            outputStream.write(dataToAdd);
            byte[] result = outputStream.toByteArray();
            blockData = result;
        }
        catch (Exception e)
        {
            System.out.println("ADDING DATA TO LEDGER ERROR: " + e);
        }
    }
*/
    public void AddData(Transaction transaction)
    {
        dataTree.AddTransaction(transaction);
    }

    public String GetMerkleRoot()
    {
        return dataTree.merkleRoot;
    }
    /*
    public byte[] GetData()
    {
        return blockData;
    }

    public boolean IsEmpty()
    {
        return blockData == null;
    }
    */
}