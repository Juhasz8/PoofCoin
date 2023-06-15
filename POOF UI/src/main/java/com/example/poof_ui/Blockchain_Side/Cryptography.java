package com.example.poof_ui.Blockchain_Side;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class Cryptography {

    // Function that takes the string input
    // and returns the hashed string.
    public static String sha256(String input)
    {
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            int i = 0;

            byte[] hash = sha.digest(input.getBytes("UTF-8"));

            // hexHash will contain
            // the Hexadecimal hash
            StringBuffer hexHash = new StringBuffer();

            while (i < hash.length)
            {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hexHash.append('0');
                hexHash.append(hex);
                i++;
            }

            return hexHash.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    //THIS METHOD IS DEPRECATED PROBABLY
    public static byte[] ConvertFromTransactionToByte(Transaction match)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
            if(match.type == TransactionType.NORMAL)
                dos.write(match.fromPublicKey.getBytes());
            dos.write(match.toPublicKey.getBytes());
            dos.writeDouble(match.amount);

            dos.flush();
        }
        catch (Exception e)
        {
            System.out.println("Converting from transaction to byte[] resulted in an error!");
            throw new RuntimeException(e);
        }

        return bos.toByteArray();
    }

    public static String ConvertFromByteToString(byte[] data)
    {
        try
        {
            return new String(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println("Converting from byte[] to string resulted in an error!");
            throw new RuntimeException(e);
        }
    }

    public static String ConvertFromTransactionToString(Transaction transaction)
    {
        return ConvertFromByteToString(ConvertFromTransactionToByte(transaction));
    }

    public static String ConvertFromTransactionToHash(Transaction transaction)
    {
        return sha256(ConvertFromTransactionToString(transaction));
    }
}