package com.polar.browser.imagebrowse;

/**

 {
 "imgs": [
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/e35/17333900_429464404073865_2305848674275557376_n.jpg",
 "width": 360,
 "height": 240,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeoAntarctica"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/e35/17332472_762552980570127_8930880786857459712_n.jpg",
 "width": 360,
 "height": 360,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/14719833_310540259320655_1605122788543168512_a.jpg",
 "username": "instagram"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/e35/17332795_1730995577211565_342358010737197056_n.jpg",
 "width": 360,
 "height": 254,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeo"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/e35/17268296_310841539333731_3737191837572530176_n.jpg",
 "width": 360,
 "height": 360,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeo"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/e35/17332321_1680050442021500_5759606265546276864_n.jpg",
 "width": 360,
 "height": 450,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeo"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/s1080x1080/e15/fr/17126756_1895123337372791_6095405517625098240_n.jpg",
 "width": 360,
 "height": 202,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeoBrooklyn, New York"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/s1080x1080/e35/17333940_222844408190215_3701984354264678400_n.jpg",
 "width": 360,
 "height": 360,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeoParque Pumalin"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/s640x640/sh0.08/e35/17333020_670293379844740_3633977077595635712_n.jpg",
 "width": 360,
 "height": 360,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeo"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/e35/17333517_429629697375189_8359949839307374592_n.jpg",
 "width": 360,
 "height": 360,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeo"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/s1080x1080/e15/fr/17332380_184259675410823_6547415044156555264_n.jpg",
 "width": 360,
 "height": 268,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeoSouth Georgia and the South Sandwich Islands"
 },
 {
 "url": "https://scontent.cdninstagram.com/t51.2885-15/e35/17267309_477044075753255_7982404372694827008_n.jpg",
 "width": 360,
 "height": 239,
 "profPic": "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13597791_261499887553333_1855531912_a.jpg",
 "username": "natgeo"
 }
 ]
 }


 */

import java.io.Serializable;

/**
 * Created by duan on 17/3/14.
 */

public class ImageInfo implements Serializable{

    public String url;

    public int width;

    public int height;

    public String profPic;

    public String username;

    public ImageInfo(){};

    public ImageInfo(String url, int width, int height, String profPic, String username) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.profPic = profPic;
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getProfPic() {
        return profPic;
    }

    public String getUsername() {
        return username;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setProfPic(String profPic) {
        this.profPic = profPic;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
