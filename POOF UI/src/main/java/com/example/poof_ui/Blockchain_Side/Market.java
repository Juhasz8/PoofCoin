package com.example.poof_ui.Blockchain_Side;

import java.util.ArrayList;

public class Market
{
    //this shouldnt be here. every user should have its own copy
    private ArrayList<Block> blockChain = new ArrayList<>();


    //(this method is for later use maybe)
    private Boolean isChainValid()
    {
        Block currentBlock;
        Block previousBlock;

        // Iterating through
        // all the blocks
        for (int i = 1; i < blockChain.size(); i++)
        {

            // Storing the current block
            // and the previous block
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i - 1);

            // Checking if the current hash
            // is equal to the
            // calculated hash or not
            if (!currentBlock.hash.equals(currentBlock.hash))
            {
                System.out.println("Hashes are not equal");
                return false;
            }

            // Checking of the previous hash
            // is equal to the calculated
            // previous hash or not
            if (!previousBlock.hash.equals(currentBlock.previousHash))
            {
                System.out.println("Previous Hashes are not equal");
                return false;
            }
        }

        // If all the hashes are equal
        // to the calculated hashes,
        // then the blockchain is valid
        return true;
    }
}
