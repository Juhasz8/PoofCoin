package com.example.poof_ui.Blockchain_Side;

//We have this class because we did not want to store the public key of the miner in the block,
//but the full node should keep track of this info, so the full node stores FullNodeBlocks in the chain
public class FullNodeBlock
{
    public Block block;

    public String luckyMinerPublicKey;

    public FullNodeBlock (Block block, String luckyMinerPublicKey)
    {
        this.block = block;
        this.luckyMinerPublicKey = luckyMinerPublicKey;
    }

}
