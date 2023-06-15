package com.example.poof_ui.Blockchain_Side;

public class MerkleNode
{

    public String hashValue;
    public MerkleNode leftChild;
    public MerkleNode rightChild;

    public MerkleNode(String hashValue, MerkleNode leftChild, MerkleNode rightChild)
    {
        this.hashValue = hashValue;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

}
