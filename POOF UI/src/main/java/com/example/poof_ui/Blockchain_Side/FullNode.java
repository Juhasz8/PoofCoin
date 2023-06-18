package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.PoofController;
import com.example.poof_ui.TrustedBlocksGUI;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EmptyStackException;


//acts as a trusted source of the blockchain
public class FullNode
{

    public FullNodeBlock block1 = new FullNodeBlock(new Block(null, null), null);

    public FullNodeBlock lastTrustedBlock;
    //this will be the blockchains and the trusted blockchain
    private ArrayList<ArrayList<FullNodeBlock>> blockChains = new ArrayList<>();

    private int lastTrustedBlockIndex = 0;

    private int blockTrustingLimit = 1;   //this means in this blockchain:  A(first block is already trusted) -> B -> C -> D   ----> B gets trusted after D is mined

    public ArrayList<Transaction> waitingTransSinceLastTrustedBlock = new ArrayList<>();

    //for rounding numbers up to 2 decimal
    protected DecimalFormat decFormatter = new DecimalFormat("0.0");

    public FullNode()
    {

    }
    public synchronized String GetLastTrustedBlockHash()
    {
        if(lastTrustedBlock == null)
            return "-";
        else
            return lastTrustedBlock.block.hash;
    }

    public synchronized void NotifyNodeThatNewBlockWasMined(FullNodeBlock newFullNodeBlock)
    {
        /*
        System.out.println("--------------------------");
        System.out.println("I JOINED THE SYNCRONISED METHOD: " + Network.getInstance().networkUsers.get(newFullNodeBlock.luckyMinerPublicKey).name);
        System.out.println("with merkle root: " + newFullNodeBlock.block.dataTree.merkleRoot);
        System.out.println("and with hash: " + newFullNodeBlock.block.hash);
        System.out.println("and with previous hash: " + newFullNodeBlock.block.previousHash);
        System.out.println("--------------------------");
        */

        //in the case of the very first blockchain that was mined and added, we just add it and trust it immediately (cause its data is empty anyways)
        if(blockChains.size() == 0)
        {
            //System.out.println("VERY FIRST BLOCK ADDED TO THE CHAIN");
            blockChains.add(new ArrayList<>(){});
            blockChains.get(0).add(newFullNodeBlock);
            TrustABlock(newFullNodeBlock);
            Network.getInstance().NotifyMinersAboutNewBlockMined(newFullNodeBlock);

            DebugPrintingOfBlockchains();
            return;
        }


        boolean conflictHappened = false;
        //check whether there is a conflict
        ArrayList<FullNodeBlock> longestChain = GetLongestChain();
        for(int i = lastTrustedBlockIndex; i < longestChain.size(); i++)
        {
            //if the new blocks previous hash matches up with any of the previous hashes of the longest chain
            if(newFullNodeBlock.block.previousHash.equals(longestChain.get(i).block.previousHash))
            {
                //System.out.println("THE NEW BLOCKK PREVIOUS HASH MATCHES WITH THAT OF INDEX: " + i + "CAUSE PREVI HASH: " + newFullNodeBlock.block.previousHash);

                // if the new block has the same data as the one that was just mined
                // -> the second guy didn't get notified yet so he mined the same block and tried to add it
                if(newFullNodeBlock.block.GetMerkleRoot().equals(longestChain.get(i).block.GetMerkleRoot()))
                {
                    //System.out.println("The poor second guy was just after the first one, so unlucky! Better luck next time!");
                    return;
                }
                else
                {
                    //System.out.println("they down have the same merkle root!: " + newFullNodeBlock.block.GetMerkleRoot() + " | " + longestChain.get(i).block.GetMerkleRoot());
                }
                //Conflict at block number I!!

                //branch apart from the current longestChain
                ArrayList<FullNodeBlock> branch = new ArrayList<>();
                for (int j=0; j < i; j++)
                {
                    branch.add(longestChain.get(j));
                }
                branch.add(newFullNodeBlock);
                blockChains.add(branch);

                System.out.println("BLOCKCHAIN CONFLICT HAPPENED! Somebody is trying to be sneaky!");
                conflictHappened = true;
            }
            //else if(i == longestChain.size()-1)
                //System.out.println("NO CONFLICT OR UNLUCKY GUY");
        }

        Network.getInstance().NotifyMinersAboutNewBlockMined(newFullNodeBlock);

        //if there were no conflicts, we just trust an old block in the past and add the new block to the chain
        if(!conflictHappened)
        {
            //and add the new block to the currect blockchain
            for (int i = 0; i < blockChains.size(); i++)
            {
                //System.out.println("checking if " + blockChains.get(i).get(blockChains.get(i).size()-1).block.hash+ " equals " + newFullNodeBlock.block.previousHash);
                //checking if the i'th blockchain contains the hash which the new block has as a previoushash
                if(blockChains.get(i).get(blockChains.get(i).size()-1).block.hash.equals(newFullNodeBlock.block.previousHash))
                {
                    //System.out.println("added a new block on an existing chain! ");
                    blockChains.get(i).add(newFullNodeBlock);
                    break;
                }
                else if(i == blockChains.size()-1)
                {
                    System.out.println("-------------------------------------");
                    System.out.println("SOMETHING WRONG WITH ADDING NEW BLOCK! ");
                    System.out.println("last hash: " + blockChains.get(i).get(blockChains.get(i).size()-1).block.hash);
                    System.out.println("new block's previous hash: " + newFullNodeBlock.block.previousHash);
                    System.out.println("-------------------------------------");
                    return;
                }
            }

            longestChain = GetLongestChain();
            //trust a block in the past
            if(longestChain.size() >= 2+blockTrustingLimit)
            {
                lastTrustedBlockIndex++;
                FullNodeBlock blockToBeTrusted = longestChain.get(lastTrustedBlockIndex);
                TrustABlock(longestChain.get(lastTrustedBlockIndex));

                //remove all the branch chains, which has a size less or equal to the longest one - difficulty
                for (int i = blockChains.size()-1; i >= 0; i--)
                {
                    //the branch chain is too short, terminate this branch!
                    if(blockChains.get(i).size() <= longestChain.size() - blockTrustingLimit)
                    {
                        blockChains.remove(i);
                    }
                }
            }
        }

        //DebugPrintingOfBlockchains();
    }



    private synchronized void DebugPrintingOfBlockchains()
    {
        System.out.println("Blockchains: ");
        for (int i = 0; i < blockChains.size(); i++)
        {
            if(i != 0)
                System.out.println();
            for (int j = 0; j < blockChains.get(i).size(); j++)
            {
                if(blockChains.get(i).get(j).block.isTrusted)
                    System.out.print(" -> " + j + "(T)");
                else
                    System.out.print(" -> " + j + "(?)");
            }
        }
        System.out.println();
        System.out.println("--------------------");
    }

    private synchronized void TrustABlockGUI(FullNodeBlock fullNodeBlock)
    {
        //creating the trusted block GUI
        TrustedBlocksGUI trustedBlocksGUI = new TrustedBlocksGUI();

        //setting the values from the fullnodeBlock
        trustedBlocksGUI.setPreviousHash(fullNodeBlock.block.previousHash);
        trustedBlocksGUI.setHashNumber(fullNodeBlock.block.hash);
        trustedBlocksGUI.setMinersPublicKey(fullNodeBlock.luckyMinerPublicKey);
        trustedBlocksGUI.setMerkleRoot(fullNodeBlock.block.GetMerkleRoot());
        trustedBlocksGUI.setBlockNumber(String.valueOf(lastTrustedBlockIndex));

        SetTransactionsTextGUI(trustedBlocksGUI, fullNodeBlock);

        PoofController.getInstance().AddTrustedBlockGUI(trustedBlocksGUI);

        //making the Trusted Block GUI visible
        //PoofController.getInstance().AddTrustedBlockGUI(trustedBlocksGUI);
    }

    public synchronized ArrayList<Transaction> GetWaitingTransactions()
    {
        return waitingTransSinceLastTrustedBlock;
    }

    private synchronized void SetTransactionsTextGUI(TrustedBlocksGUI trustedBlocksGUI, FullNodeBlock fullNodeblock)
    {
        for (int i = 0; i < fullNodeblock.block.dataTree.transactions.size(); i++)
        {
            String transactionsText = "";
            Transaction trans = fullNodeblock.block.dataTree.transactions.get(i);
            transactionsText += Network.getInstance().networkUsers.get(trans.fromPublicKey).name;
            transactionsText += " sent ";
            transactionsText += decFormatter.format(trans.amount);
            transactionsText += " --> ";
            transactionsText += Network.getInstance().networkUsers.get(trans.toPublicKey).name;
            PoofController.getInstance().AddTrustedBlockTransactionGUI(trustedBlocksGUI, transactionsText);
        }
        //trustedBlocksGUI.setTransactionLabel(transactionsText);
    }

    private synchronized void TrustABlock(FullNodeBlock fullNodeblock)
    {
        lastTrustedBlock = fullNodeblock;
        fullNodeblock.block.isTrusted = true;

        //creating the trusted block UI
        TrustABlockGUI(fullNodeblock);

        //miner gets reward
        Network.getInstance().networkUsers.get(fullNodeblock.luckyMinerPublicKey).IncreaseWallet(Network.getInstance().GetMinerReward());

        //make the transactions actually go through and appear in the users' wallets
        for(int i = 0; i < fullNodeblock.block.dataTree.transactions.size(); i++)
        {
            Transaction transaction = fullNodeblock.block.dataTree.transactions.get(i);

            if(waitingTransSinceLastTrustedBlock.contains(transaction))
                waitingTransSinceLastTrustedBlock.remove(transaction);
            else
                System.out.println("Something went wrong! The full node doesn't recognize a transaction!");

            if(transaction.type == TransactionType.NORMAL)
            {
                User fromUser = Network.getInstance().networkUsers.get(transaction.fromPublicKey);
                //from the user the actual amount is getting deducted
                fromUser.IncreaseWallet(-transaction.amount);
                fromUser.amountOfPendingRequests--;
            }

            //the buyer gets the expected amount
            User toUser = Network.getInstance().networkUsers.get(transaction.toPublicKey);
            toUser.IncreaseWallet(transaction.amount);
            toUser.amountOfPendingRequests--;

            //miner gets the fee
            Network.getInstance().networkUsers.get(fullNodeblock.luckyMinerPublicKey).IncreaseWallet(transaction.fee);
        }
        //System.out.println("waiting transactions size after trusting: " + waitingTransSinceLastTrustedBlock.size());
    }

    public synchronized ArrayList<FullNodeBlock> GetLongestChain() throws IndexOutOfBoundsException
    {
        try
        {
            int longestSoFar = 1;
            ArrayList<FullNodeBlock> longestChainSoFar = blockChains.get(0);
            for (int i = 0; i < blockChains.size(); i++)
            {
                if(blockChains.get(i).size() > longestSoFar)
                {
                    longestSoFar = blockChains.get(i).size();
                    longestChainSoFar = blockChains.get(i);
                }
            }
            return longestChainSoFar;
        }
        catch (IndexOutOfBoundsException e)
        {
            throw e;
        }
    }

    public synchronized int GetLongestChainSize()
    {
        try
        {
            ArrayList longestChain = GetLongestChain();
            return longestChain.size();
        }
        catch (IndexOutOfBoundsException e)
        {
            return 0;
        }
    }

}
