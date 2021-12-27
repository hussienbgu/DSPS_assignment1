
public class Product {
    public String name = "name";
    public String title;
    public Review[] reviews;
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (Review r : reviews)
        {
            s.append(r.toString()+"\n");
        }
        return ("Title: "+title+"\nReviews:\n"+s.toString());
    }
}