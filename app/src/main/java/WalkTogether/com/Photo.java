package WalkTogether.com;

public class Photo {
    private String ImageFileName;
    private String mood;

    public Photo(String imageFileName, String mood)
    {
        this.ImageFileName = imageFileName;
        this.mood = mood;
    }
    public String getImageFileName()
    {
        return this.ImageFileName;
    }
    public String getmood()
    {
        return this.mood;
    }

    public void setImageFileName(String imageFileName) {
        ImageFileName = imageFileName;
    }
    public void setLatitude(String content) { this.mood = content;}

}
