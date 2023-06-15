package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.PoofController;
import com.example.poof_ui.TrustedBlocksGUI;

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

    private int blockTrustingLimit = 2;   //this means in this blockchain:  A(first block is already trusted) -> B -> C -> D   ----> B gets trusted after D is mined

    public ArrayList<Transaction> waitingTransSinceLastTrustedBlock = new ArrayList<>();


    public FullNode()
    {

    }
    public String GetLastTrustedBlockHash()
    {
        if(lastTrustedBlock == null)
            return "-";
        else
            return lastTrustedBlock.block.hash;
    }

    public synchronized void NotifyNodeThatNewBlockWasMined(FullNodeBlock newFullNodeBlock)
    {
        System.out.println("I JOINED THE SYNCRONISED METHOD: " + newFullNodeBlock.luckyMinerPublicKey);
        System.out.println("with merkle root: " + newFullNodeBlock.block.dataTree.merkleRoot);
        //in the case of the very first blockchain that was mined and added, we just add it and trust it immediately (cause its data is empty anyways)
        if(blockChains.size() == 0)
        {
            System.out.println("VERY FIRST BLOCK ADDED TO THE CHAIN");
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
            if(newFullNodeBlock.block.previousHash == longestChain.get(i).block.previousHash)
            {
                // if the new block has the same data as the one that was just mined
                // -> the second guy didn't get notified yet so he mined the same block and tried to add it
                if(newFullNodeBlock.block.GetMerkleRoot() == longestChain.get(i).block.GetMerkleRoot())
                {
                    System.out.println("The poor second guy was just after the first one, so unlucky! Better luck next time!");
                    return;
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
            else if(i == longestChain.size()-1)
                System.out.println("NO CONFLICT OR UNLUCKY GUY");
        }

        Network.getInstance().NotifyMinersAboutNewBlockMined(newFullNodeBlock);

        //if there were no conflicts, we just trust an old block in the past and add the new block to the chain
        if(!conflictHappened)
        {
            //and add the new block to the currect blockchain
            for (int i = 0; i < blockChains.size(); i++)
            {
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
            if(longestChain.size() >= 4)
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

        DebugPrintingOfBlockchains();
    }

    private void DebugPrintingOfBlockchains()
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

    private void TrustABlock(FullNodeBlock fullNodeblock)
    {
        lastTrustedBlock = fullNodeblock;
        fullNodeblock.block.isTrusted = true;

        String previousHash = GetLastTrustedBlockHash();

        TrustedBlocksGUI trustedBlocksGUI = new TrustedBlocksGUI();
        trustedBlocksGUI.setPreviousHash(previousHash);
        //trustedBlocksGUI.setHashNumber(fullNodeBlock.block.hash);
        //trustedBlocksGUI.setMinersPublicKey(fullNodeBlock.luckyMinerPublicKey);
        //trustedBlocksGUI.setMerkleRoot(fullNodeBlock.block.GetMerkleRoot());
        trustedBlocksGUI.setBlockNumber(String.valueOf(blockChains.size()-1));
        PoofController.getInstance().AddTrustedBlockGUI(trustedBlocksGUI);

        //we basically do all the transactions on this block ledger
        //so everyone gets his money and poffcoin, and the miner gets the rewards and fee aswell
        //so here, you just check for the transaction YOU are involved in, the dont give a shit about the others.!!!

        //we have to think about it tho, how we prevent someone from trying to spend more poffcoin than they have

        //ByteArrayInputStream bos = new ByteArrayInputStream(block.GetData());
        //DataInputStream dos = new DataInputStream(bos);

        //miner gets reward
        Network.getInstance().networkUsers.get(fullNodeblock.luckyMinerPublicKey).IncreaseWallet(Network.getInstance().GetMinerReward());

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
            }

            //the buyer gets the expected amount
            User toUser = Network.getInstance().networkUsers.get(transaction.toPublicKey);
            toUser.IncreaseWallet(transaction.amount);

            //miner gets the fee
            Network.getInstance().networkUsers.get(fullNodeblock.luckyMinerPublicKey).IncreaseWallet(transaction.fee);
        }
    }

    public ArrayList<FullNodeBlock> GetLongestChain() throws IndexOutOfBoundsException
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

    public int GetLongestChainSize()
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
//wow