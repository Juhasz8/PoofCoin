package com.example.poof_ui.Blockchain_Side;

import com.example.poof_ui.MinerGUI;
import com.example.poof_ui.PoofController;

import java.util.*;
import java.security.Signature;



enum MinerType { THAT_ONE_GUY, THESE_GUYS, GROUP, SMALL_CORP, HUGE_CORP }

public class Miner extends User
{
    private float miningPower;
    private Random rand;
    int numberOfTransactions = 0;
    private ArrayList<Transaction> waitingTrans = new ArrayList<>();

    //mining
    private Block myblock;
    private int nonce;

    //this is the previous-trusted hash, which means, if the last block was mined by me, it's the hash of that block.
    //if it was mined by someone else, and that block matches up with my block, it becomes that blocks hash.
    //if it was mined by someone else, but that block doesn't match up, the previous trusted hash doesn't change
    private String previousTrustedHash;

    public MinerType type;

    private MinerGUI minerGUI;

    private long sleepTime;

    //just for debugging until we have actual names for miners
    private int indexInUsersList;

    //this constructor should probably only take the actual mining power and the type of the miner. The power is calculated in the simulationmanager
    public Miner(MinerType type, long sleepTime)
    {
        super();
        rand = new Random();
        this.type = type;
        //miningPower = minPower + rand.nextInt(maxPower-minPower); // -> generate a random number between minPower and maxPower
        //miningPower = 10;
        this.miningPower = (1/(float)sleepTime * 10000);
        System.out.println("POWER: " + miningPower);

        this.sleepTime = sleepTime;
        indexInUsersList = Network.getInstance().networkUsers.size();
        //Update();
        Network.getInstance().JoinMinerToTheNetwork(this);

        //we might not want the last trusted block here ?
        //or maybe he will take the last trusted block, create his own blocks, but instead of mining,
        //he just compares to the untrusted blocks and if he gets to the same conclusion then on one of the chains, then he follows up on that chain
        myblock = new Block(null, Network.getInstance().fullNode.GetLastTrustedBlockHash());
        previousTrustedHash = Network.getInstance().fullNode.GetLastTrustedBlockHash();
        //myblock.AddData(Cryptography.ConvertFromTransactionToByte(new TransactionMatch(TransactionType.REWARD, null, publicKeyString, Network.getInstance().GetMinerReward())));
        //myblock.AddData(new Transaction(TransactionType.REWARD, null, publicKeyString, Network.getInstance().GetMinerReward()));

        ArrayList<Transaction> waitingFullNodeTrans = Network.getInstance().fullNode.waitingTransSinceLastTrustedBlock;
        //adding the waiting transactions from the full node to the ledger
        for(int i = waitingFullNodeTrans.size()-1; i >= 0; i--)
        {
            if(numberOfTransactions < Network.getInstance().GetMaxLedgerCount())
            {
                ProcessLedger(waitingFullNodeTrans.get(i));
            }
            else
                break;
        }

        minerGUI = new MinerGUI();
        PoofController.getInstance().SetMinerGUICoin(minerGUI, decFormatter.format(0));
        PoofController.getInstance().AddMinerGUI(minerGUI, decFormatter.format(miningPower));
    }


    public static Miner getMiner() {
        // Create separate categories of miners

        /*
        List<Miner> minerCategories = Arrays.asList(
        new Miner(100,MinerType.HUGE_CORP, 100),
        new Miner(50, MinerType.SMALL_CORP, 100),
        new Miner(30, MinerType.GROUP, 100),
        new Miner(2, MinerType.THESE_GUYS, 100),
        new Miner(1,MinerType.THAT_ONE_GUY, 100)
        );


        // Create the chances of getting picked
        Map<String, List<Integer>> dictionary = Map.of(
        "BigCompany", List.of(0,6),
        "SmallCompany", List.of(6,16),
        "BigGroup", List.of(16,31),
        "SmallGroup", List.of(31,61),
        "Individual", List.of(61,101)
        );

        Random randomNumber  = new Random();
        int myRandomNumber  = randomNumber.nextInt(0,101);
        String myKey = "";

        for (Map.Entry<String, List<Integer>> entry : dictionary.entrySet()) {
            String key = entry.getKey();
            List<Integer> value = entry.getValue();

            if (myRandomNumber > value.get(0) && myRandomNumber < value.get(1)) {
                myKey = key; // Set myKey to the corresponding key value
            }
        }

        if (myKey.equals("BigCompany")) {
            return minerCategories.get(0);
        } else if (myKey.equals("SmallCompany")) {
            return minerCategories.get(1);
        } else if (myKey.equals("BigGroup")) {
            return minerCategories.get(2);
        } else if (myKey.equals("SmallGroup")) {
            return minerCategories.get(3);
        } else if (myKey.equals("Individual")) {
            return minerCategories.get(4);
        }
        return null;
        */
        return null;
    }

    public void run()
    {
        synchronized (this)
        {
            while (true)
            {
                try
                {
                    while(isSuspended)
                        wait();

                    if(cycleUntilPossibleNextExchange > 0)
                        cycleUntilPossibleNextExchange--;

                    TryToMine();
                    DecideToSell();
                    //DecideToSell();
                    //System.out.println("check");
                    //Thread.sleep((1/miningPower) * 1000);
                    //Thread.currentThread().wait((long)0.25);

                    Thread.sleep(sleepTime);

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    private void TryToMine()
    {
        String target = new String(new char[Network.getInstance().GetDifficulty()]).replace("\0", "0");

        String hash = CalculateHash();

        //System.out.println("Tried to mine, result: " + hash);
        //minerGUI.SetHashText(hash);
        PoofController.getInstance().SetMinerGUIHash(minerGUI, hash);

        if(hash.substring(0, Network.getInstance().GetDifficulty()).equals(target))
        {
            //New Block mined successfully!!
            System.out.println("--------------------");
            System.out.println("I mined a block successfully!! " + name + "_" + indexInUsersList);
            myblock.BlockMined(hash);

            //we have to notify everyone on the network that we are the lottery winners
            Network.getInstance().NewBlockWasMined(myblock, publicKeyString);

            ITrustANewBlock(hash);


        }
        else
        {
            nonce++;
        }
    }


    private String CalculateHash()
    {
        //in real life the propagation of the information that a new block was mined is one of the things that make sure
        //that the miners are not trying to guess the same, but since in our simulation the propagation is 0,
        //to make sure they are guessing different numbers they also include their unique public key to make sure they are all getting different hashes
        return Cryptography.sha256(Long.toString(myblock.timeStamp) + Integer.toString(nonce) + myblock.GetMerkleRoot()) + publicKeyString;
    }

    //private void ValidateBlock

    //here we broadcast the new block to everyone execute all transactions stated on the ledger
    public boolean VerifySignedMessage(Transaction signedTransaction)
    {
        //byte[] originalMessage, byte[] signedMessage, PublicKey sellerPublicKey
        try
        {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(signedTransaction.seller.publicKey);
            signature.update(signedTransaction.originalMessage);
            boolean isCorrect = signature.verify(signedTransaction.signedMessage);

            return isCorrect;
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return false;
    }

    private long GetSleepingTime(int x)
    {
        //return (long)-0.018*x + (long)1.9; //sleeping time should be 1 sec if power is 10 and 0.1 sec if power 100
        return 1/x;
    }

    private void ITrustANewBlock(String correctHash)
    {

        previousTrustedHash = correctHash;
        //resetting the nonce
        nonce = 0;

        //flushing the data
        myblock = new Block(null, previousTrustedHash);
        //myblock.AddData(new Transaction(TransactionType.REWARD, null, publicKeyString, Network.getInstance().GetMinerReward()));

        //adding the waiting transactions to the ledger
        for(int i = waitingTrans.size()-1; i >= 0; i--)
        {
            if(numberOfTransactions < Network.getInstance().GetMaxLedgerCount())
            {
                ProcessLedger(waitingTrans.get(i));
                waitingTrans.remove(i);
            }
            else
                break;
        }

        //everyone else will add this block on the blockchain, while keeping in mind that it might cause conflicts


        //A -> B -> C -> D -> E -> F

        //A -> B -> C  -> D*

        //the way we will deal with conflicts, is the following:
        //we will only compute the transactions, whenever it becomes a trusted block.
        //otherwise we will just wait until new blocks are mined


        //if a miner who is unlucky and didn't mine the block, gets notified of a block that was mined
        //he will check if that block is the same that he has. If they match up, he will discard his current block
        //because from his point of view, in his belief, that block which was just mined is going to become a trusted block later.
        //otherwise, he will keep working on his current block, because the block which was just mined was either a sneaky block, or this miner is working on a sneaky block himself#



        //the miner getting reward maybe shouldnt be on the ledger because it fucks up the merkle root check in the case above?
        //it wont be on the ledger!! the full node will handle giving out the gift rewards once a block becomes trusted!!!!!!!!!!

    }

    //checks whether u should stop mining cause the chain you are working on was called "untrusted" by the fullnode
    //this is for the cheathers, it checks whether they still have any chance left or not
    public void CheckMyTrustedBlockGettingDiscarded()
    {

    }

    public void SomebodyElseMinedABlock(Block newBlock)
    {
        //the previous hash will be updated according to the relation between my current block I am working on and this new block that was mined by someone else
        //we do this check simply by comparing the merkle root of the new block that was mined and the block I am trying to mine
        System.out.println("I am Mr_" + indexInUsersList + " somebody else mined a new block! ");

        //check if the new block mined by someone else is trusted by you or not
        if(newBlock.GetMerkleRoot() != myblock.GetMerkleRoot())
        {
            System.out.println("I am Mr_" + indexInUsersList + " I DON'T trust the new block! ");
            //either the miner of the new block is trying to cheat or this miner is trying to cheat
            //so you keep mining your block
            return;
        }
        System.out.println("I am Mr_" + indexInUsersList + " I trust the new block! ");

        //this miner trusts the block that was mined by someone else because it contains the same transactions
        ITrustANewBlock(newBlock.hash);



    }

    public void ProcessLedger(Transaction signedTransaction)
    {
        //byte[] originalMessage, byte[] signedMessage, PublicKey sellerPublicKey
        if (numberOfTransactions < Network.getInstance().GetMaxLedgerCount())
        {
            if (VerifySignedMessage(signedTransaction))
            {
                myblock.AddData(signedTransaction);
                numberOfTransactions++;
            }
            else
            {
                System.out.println("Unverified transaction");
            }
        }
        else
        {
            waitingTrans.add(signedTransaction);
        }
    }

    private void DecideToSell()
    {
        //if it's not the first transaction, there is a 10% chance that the miner wont sell anything
        //if he recently sold he is on a break
        //if he has no poof-s he won't even think about selling
        if(cycleUntilPossibleNextExchange > 0 || hypotheticalPoofWallet == 0 || random.nextInt(10) == 0)
            return;

        Exchange exchange = new Exchange();

        if(type == MinerType.THAT_ONE_GUY)
        {
            //random difference between -100 and 10
            exchange.difference = random.nextInt(110)-100;

        }
        else if (type == MinerType.THESE_GUYS)
        {
            //random difference between -75 and 10
            exchange.difference = random.nextInt(85)-75;
        }
        else if (type == MinerType.GROUP)
        {
            //random difference between -50 and 10
            exchange.difference = random.nextInt(60)-50;
        }
        else if (type == MinerType.SMALL_CORP)
        {
            //random difference between -35 and 10
            exchange.difference = random.nextInt(45)-35;
        }
        else if (type == MinerType.HUGE_CORP)
        {
            //random difference between -25 and 10
            exchange.difference = random.nextInt(35)-25;
            //selling between 15% and 50%
            exchange.percent = random.nextDouble(0.35)+0.15;
        }


        //every type's decision is influenced by the current event and the trend
        CalculateNormalSellingInfluences(exchange);

        //there is a 20% chance for the trader to make a bigger decision
        if(random.nextInt(5) == 0)
        {
            exchange.difference *= 1.5;
        }

        //everyone has a 5% chance of deciding based on complete randomness, like a psychopath
        if(random.nextInt(20) == 0)
        {
            //completely random behaviour

            //random difference between -100 and 25
            exchange.difference = random.nextInt(150)-125;
            //will deal with 5-95% of his current currency
            exchange.percent = random.nextDouble(.9)+0.05;
        }


        //the influence of everything has to be big enough for the miner to sell
        if(exchange.difference <= -25)
            MakeTheSellRequest(exchange);
    }

    //this is called by the miners since they can only sell
    protected void CalculateNormalSellingInfluences(Exchange exchange)
    {
        //if there are only a few miners, they are more likely to sell than at a later point when there will be more miners
        if(Network.getInstance().GetMinerAmount() < 3)
        {
            exchange.difference -= 35;
            //they sell between 60% and 100% of their puff
            exchange.percent = random.nextDouble(0.4)+0.6;
        }
        else if(Network.getInstance().GetMinerAmount() < 10)
        {
            exchange.difference -= 20;
            //they sell between 60% and 100% of their puff
            exchange.percent = random.nextDouble(0.4)+0.6;
        }

        //event influences

    }

    private void MakeTheSellRequest(Exchange exchange)
    {
        //double feePercent = random.nextDouble(5)+2;
        double feePercent = random.nextDouble(0.05)+0.02; //feePercent is a random between 2 and 7

        double amountToSell = hypotheticalPoofWallet * Math.max((exchange.percent+(exchange.difference/10)), 1);

        System.out.println("I AM A MINER AND I REQUESTED TO SELL: " + amountToSell);
        //calculate the actual amount based on difference and exchangePercent
        RequestToSell(amountToSell * (1-feePercent), amountToSell * feePercent);

        hypotheticalPoofWallet -= amountToSell;
        //this miner won't sell for the next 1-3 turns (2-6 sec)
        cycleUntilPossibleNextExchange = random.nextInt(3)+1;
    }

    @Override
    public void IncreaseWallet(double amount)
    {
        super.IncreaseWallet(amount);
        PoofController.getInstance().SetMinerGUICoin(minerGUI, decFormatter.format(poofWallet));
    }
}
