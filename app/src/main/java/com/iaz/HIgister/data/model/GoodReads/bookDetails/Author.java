package com.iaz.HIgister.data.model.GoodReads.bookDetails;

public class Author
{
    private String id;

    private String name;

    private String link;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getLink ()
    {
        return link;
    }

    public void setLink (String link)
    {
        this.link = link;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", name = "+name+", link = "+link+"]";
    }
}
