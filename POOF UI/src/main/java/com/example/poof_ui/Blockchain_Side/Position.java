package com.example.poof_ui.Blockchain_Side;

public class Position
{
    public int x;
    public int y;

    public Position(Position pos)
    {
        this.x = pos.x;
        this.y = pos.y;
    }
    public Position (int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public Position Add (Position pos)
    {
        x += pos.x;
        y += pos.y;
        return this;
    }
}
