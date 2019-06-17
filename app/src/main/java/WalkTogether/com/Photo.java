package WalkTogether.com;

public class Photo {
    private String ImageFileName;
    private String sentence;

    public Photo(String imageFileName, String mood)
    {
        this.ImageFileName = imageFileName;
        this.sentence = mood;
    }
    public String getImageFileName()
    {
        return this.ImageFileName;
    }
    public String getmood()
    {
        return this.sentence;
    }

    public void setImageFileName(String imageFileName) {
        ImageFileName = imageFileName;
    }
    public void setLatitude(String content) { this.sentence = content;}

}
