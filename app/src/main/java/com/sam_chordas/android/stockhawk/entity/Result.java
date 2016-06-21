package com.sam_chordas.android.stockhawk.entity;

public class Result
{
    private Query query;

    public Query getQuery ()
    {
        return query;
    }


    public void setQuery (Query query)
    {
        this.query = query;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [query = "+query+"]";
    }
}
