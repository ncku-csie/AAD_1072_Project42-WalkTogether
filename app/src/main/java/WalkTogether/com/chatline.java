package WalkTogether.com;

public class chatline {

    private String People;
    private String Sentence ;

    public chatline(String people, String sentence)
    {
        People = people;
        Sentence = sentence;
    }
    public String getpeople()
    {
        return People;
    }
    public String getsentence()
    {
        return Sentence;
    }

    public void setpeople(String people) { People = people; }
    public void setsentence(String sentence) { Sentence = sentence; }
}
