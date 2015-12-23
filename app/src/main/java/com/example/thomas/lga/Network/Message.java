package com.example.thomas.lga.Network;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 13.12.2015.
 */
public abstract class Message
{
    protected byte[] header;
    protected byte[] content;
    protected int id;

    public byte[] getContent()
    {
        return content;
    }

    // size in bytes without id

    public int getSize()
    {
        return content.length;
    }

    public int getId()
    {
        return id;
    }

    public byte[] getBytes()
    {
        updateContent();
        ByteBuffer buffer = ByteBuffer.allocate(4 + content.length);
        buffer.putInt(id);
        buffer.put(content);
        return buffer.array();
    }

    public abstract void updateContent();
}
