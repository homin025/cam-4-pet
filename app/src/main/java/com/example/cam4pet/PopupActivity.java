package com.example.cam4pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PopupActivity extends AppCompatActivity {

    TextView productName;
    TextView productPrice;
    TextView productCompany;
    TextView productIngre;
    ImageButton button;
    ImageButton ok_button;
    ImageView popupImg;

    int checkNum = 0;
    int num =0;
    int btnValue;
    int checkDogCat;
    //dog - 0, cat - 1

    String[][] dogFood = {
            {"The Real 더리얼 그레이프리 크런치 닭고기", "ANF 산전연령 유기농 6Free 연어 애견사료", "에코1 유기농 양고기 애견사료"},
            {"53990won", "40000won", "19000won"},
            {"하림", "대산앤컴퍼니", "네츄럴코어"},
            {"닭고기, 병아리콩, 타피오카전분..", "연어, 닭고기, 쌀, 현미..", "양고기, 시금치, 고구마 ..."}
    };
    String [][] dogSnack = {
            {"밀크껌 대", "부드러운 덴탈껌 강아지 치석제거", "강아지 트릿"},
            {"9900won", "9800won", "23,700won"},
            {"굿프랜드", "피피픽", "반려소반"},
            {"우피, 우유향", "쌀, 타피오카 전분, 콜라겟, 치킨펫..", "닭가슴살 100%"}
    };

    String [][] dogToy = {
            {"라텍스 스포츠 반려견 장난감", "딩동펫 반려동물 터그장난감 시바", "딩동펫 반려견 공룡알 간식볼"},
            {"5,720won", "8,800won", "8,800won"},
            {"제제펫", "딩동펫", "딩동펫"},
            {"라텍스..", "폴라폴리스, 솜..", "PP.."}
    };

    String [][] dogUrls = {
            {"http://www.11st.co.kr/products/2986910160?utm_medium=%EA%B2%80%EC%83%89&gclid=Cj0KCQiAnb79BRDgARIsAOVbhRooYRJeWoQXRcmN-KaGPKCZHxJpp2_KcuXyItUGGA8n74DN_RNYmb0aAmf8EALw_wcB&utm_source=%EA%B5%AC%EA%B8%80_PC_S_%EC%87%BC%ED%95%91&utm_campaign=%EA%B5%AC%EA%B8%80%EC%87%BC%ED%95%91PC+%EC%B6%94%EA%B0%80%EC%9E%91%EC%97%85&utm_term=",
            "https://www.coupang.com/vp/products/3160116?itemId=164444028&vendorItemId=3000023962&pickType=COU_PICK&q=%EA%B0%95%EC%95%84%EC%A7%80%EC%82%AC%EB%A3%8C&itemsCount=36&searchId=f34956bcf9e84c02820ccdbe40bffed5&rank=6&isAddedCart=",
            "https://www.coupang.com/vp/products/298462?itemId=693454&vendorItemId=3000546486&q=%EA%B0%95%EC%95%84%EC%A7%80%EC%82%AC%EB%A3%8C&itemsCount=36&searchId=378c587a7fdd44e88646df1f3ecd1b19&rank=38&isAddedCart="},

            {"https://www.coupang.com/vp/products/119602765?itemId=356438632&vendorItemId=72082715868&q=%EA%B0%9C+%EB%BC%88%EB%8B%A4%EA%B7%80&itemsCount=36&searchId=b35f91e866a54201afe1f409b1516f2d&rank=3&isAddedCart=",
            "http://item.gmarket.co.kr/Item?goodscode=1833375553",
            "https://www.coupang.com/vp/products/1708583408?vendorItemId=70896476618&rmdId=ed055da9a1b244aa967c65c9b1c3743c&eventLabel=recommendation_widget_pc_srp_001&platform=web&rmdABTestInfo=8088:A,9266:C,8091:A,9437:B,8534:A&rmdValue=p2170468692:vt-1.0.0:p1708583408&isAddedCart="},

            {"https://www.coupang.com/vp/products/89773366?itemId=280059428&vendorItemId=3683858493&q=%EA%B0%9C%EC%9E%A5%EB%82%9C%EA%B0%90&itemsCount=36&searchId=683aca75eba246d8aa65c0d4b7cdadc7&rank=12&isAddedCart=",
            "https://www.coupang.com/vp/products/332331188?vendorItemId=5540675774&sourceType=SDP_ALSO_VIEWED&rmdId=9c2b23587c35418bbd91d20d156e13c9&eventLabel=recommendation_widget_pc_sdp_001&platform=web&rmdABTestInfo=8088:A,9266:C,8091:A,9437:B,8534:A&rmdValue=p22027343:vt-1.0.0:p332331188&isAddedCart=",
            "https://www.coupang.com/vp/products/95504742?itemId=294636274&vendorItemId=3724976207&q=%EA%B0%95%EC%95%84%EC%A7%80+%EC%9E%A5%EB%82%9C%EA%B0%90&itemsCount=36&searchId=02c363550c5548908aa6e5ea701e0642&rank=7&isAddedCart="}
    };

    String[][] catFood = {
            {"쉬바 고양이 간식캔 그레이비소스 참치", "탐사 전연령용 고양이 사료", "전연령 밥이보약 고양이사료"},
            {"29,570won", "29,290won", "29,100won"},
            {"쉬바", "탐사", "하림펫푸드"},
            {"참치, 치어, 그레이비소스..", "닭고기, 토마토, 사과..", "닭고기, 완두콩, 비트식이섬유.."}
    };
    String [][] catToy = {
            {"고양이용 쥐 장난감 3p", "고양이 낚싯대 장난감 세트", "마우스 무빙 장난감"},
            {"8800won", "9800won", "10,900won"},
            {"에이비엠", "딩동펫", "딩동펫"},
            {"천, 폴리..", "우드, 폴리, 깃털..", "PP, 천.."}
    };

    String [][] catHouse = {
            {"반려동물 소프트 마약 방석", "딩동펫 고양이 공룡알 숨숨 하우스", "숨숨터널 고양이 터널 하우스 장난감"},
            {"13,500won", "17,800won", "29,900won"},
            {"다원", "딩동펫", "세븐펫"},
            {"라텍스, 솜(충전재)..", "폴리에스테르..", "옥스포드 원단.."}
    };

    String [][] catUrls = {
            {"https://www.coupang.com/vp/products/188253363?itemId=537650088&vendorItemId=4403442988&sourceType=SDP_BOUGHT_TOGETHER&isAddedCart=",
            "https://www.coupang.com/vp/products/1173188814?itemId=2151439093&vendorItemId=70149742855&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%82%AC%EB%A3%8C&itemsCount=36&searchId=b980d5a8074c4c1e8b801ccd2e62e7f6&rank=2&isAddedCart=",
            "https://www.coupang.com/vp/products/1349593999?itemId=2377637378&vendorItemId=70373191035&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%82%AC%EB%A3%8C&itemsCount=36&searchId=2656502fc80845e2865e0a2ea7e82f70&rank=20&isAddedCart="},

            {"https://www.coupang.com/vp/products/106167267?itemId=321207714&vendorItemId=3786954439&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%A5%90&itemsCount=36&searchId=9f0b0fb6e6204c6fb1bde1631fbb3187&rank=3&isAddedCart=",
            "https://www.coupang.com/vp/products/100377746?itemId=306587090&vendorItemId=3756096348&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%9E%A5%EB%82%9C%EA%B0%90&itemsCount=36&searchId=f53294a45df64f3fbd1d027c31f554c8&rank=2&isAddedCart=",
            "https://www.coupang.com/vp/products/33079626?itemId=124383631&vendorItemId=3253372782&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%9E%A5%EB%82%9C%EA%B0%90&itemsCount=36&searchId=35dccb8e5bbc4be68a3bc4908dcec35a&rank=36&isAddedCart="},

            {"https://www.coupang.com/vp/products/1925548340?itemId=3268950607&vendorItemId=72481925659&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%BF%A0%EC%85%98&itemsCount=36&searchId=2701d9ce06494e3fa7f03fadbe00cf2c&rank=12&isAddedCart=",
            "https://www.coupang.com/vp/products/108739142?itemId=329170274&vendorItemId=3807270377&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%88%A8%EC%88%A8%EC%A7%91&itemsCount=36&searchId=637064cf422346a38add83599d35e7bc&rank=1&isAddedCart=",
            "https://www.coupang.com/vp/products/313492462?itemId=991346752&vendorItemId=5417089944&q=%EA%B3%A0%EC%96%91%EC%9D%B4+%EC%88%A8%EC%88%A8%EC%A7%91&itemsCount=36&searchId=637064cf422346a38add83599d35e7bc&rank=2&isAddedCart="}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productCompany = findViewById(R.id.productCom);
        productIngre = findViewById(R.id.productIngre);
        popupImg = findViewById(R.id.popupImg);

        button = findViewById(R.id.button);
        ok_button = findViewById(R.id.button2);


        Intent intent2 = getIntent();
        btnValue = intent2.getExtras().getInt("kinds");
        num = intent2.getExtras().getInt("num");
        checkDogCat = intent2.getExtras().getInt("checkDogCat");

        setName(checkDogCat, btnValue, num);
        setImg(checkDogCat, btnValue, num);
        setBtn(checkDogCat, btnValue,num);

        ok_button.setOnClickListener(view -> {
            finish();
        });
    }

    public void setName(int checkDogCat, int btnValue, int num){
        if(checkDogCat == 0){
            switch (btnValue){
                case 0:
                    productName.setText("상품명: "+ dogFood[0][num]);
                    productPrice.setText("가격: " + dogFood[1][num]);
                    productCompany.setText("회사명: " + dogFood[2][num]);
                    productIngre.setText("원재료: "+ dogFood[3][num]);
                    break;
                case 1:
                    productName.setText("상품명: "+ dogSnack[0][num]);
                    productPrice.setText("가격: " + dogSnack[1][num]);
                    productCompany.setText("회사명: " + dogSnack[2][num]);
                    productIngre.setText("원재료: "+ dogSnack[3][num]);
                    break;
                case 2:
                    productName.setText("상품명: "+ dogToy[0][num]);
                    productPrice.setText("가격: " + dogToy[1][num]);
                    productCompany.setText("회사명: " + dogToy[2][num]);
                    productIngre.setText("원재료: "+ dogToy[3][num]);
                    break;
            }
        }
        else {
            switch (btnValue){
                case 0:
                    productName.setText("상품명: "+ catFood[0][num]);
                    productPrice.setText("가격: " + catFood[1][num]);
                    productCompany.setText("회사명: " + catFood[2][num]);
                    productIngre.setText("원재료: "+ catFood[3][num]);
                    break;
                case 1:
                    productName.setText("상품명: "+ catToy[0][num]);
                    productPrice.setText("가격: " + catToy[1][num]);
                    productCompany.setText("회사명: " + catToy[2][num]);
                    productIngre.setText("원재료: "+ catToy[3][num]);
                    break;
                case 2:
                    productName.setText("상품명: "+ catHouse[0][num]);
                    productPrice.setText("가격: " + catHouse[1][num]);
                    productCompany.setText("회사명: " + catHouse[2][num]);
                    productIngre.setText("원재료: "+ catHouse[3][num]);
                    break;
            }
        }
    }

    public void setImg( int checkDogCat, int btnValue, int num){
        if(checkDogCat == 0){//about Dog, SetImageView
            if(btnValue == 0){
                switch (num){
                    case 0: popupImg.setImageResource(R.drawable.img_dog_food_01);
                        break;
                    case 1: popupImg.setImageResource(R.drawable.img_dog_food_02);
                        break;
                    case 2: popupImg.setImageResource(R.drawable.img_dog_food_03);
                        break;
                }
            }
            else if(btnValue == 1){
                switch (num){
                    case 0: popupImg.setImageResource(R.drawable.img_dog_snack_01);
                        break;
                    case 1: popupImg.setImageResource(R.drawable.img_dog_snack_02);
                        break;
                    case 2: popupImg.setImageResource(R.drawable.img_dog_snack_03);
                        break;
                }
            }
            else {
                switch (num){
                    case 0: popupImg.setImageResource(R.drawable.img_dog_toy_01);
                        break;
                    case 1: popupImg.setImageResource(R.drawable.img_dog_toy_02);
                        break;
                    case 2: popupImg.setImageResource(R.drawable.img_dog_toy_03);
                        break;
                }
            }
        }
        else{//about Cat, SetImageView
            if(btnValue == 0){
                switch (num){
                    case 0: popupImg.setImageResource(R.drawable.img_cat_food_01);
                        break;
                    case 1: popupImg.setImageResource(R.drawable.img_cat_food_02);
                        break;
                    case 2: popupImg.setImageResource(R.drawable.img_cat_food_03);
                        break;
                }
            }
            else if(btnValue == 1){
                switch (num){
                    case 0: popupImg.setImageResource(R.drawable.img_cat_toy_01);
                        break;
                    case 1: popupImg.setImageResource(R.drawable.img_cat_toy_02);
                        break;
                    case 2: popupImg.setImageResource(R.drawable.img_cat_toy_03);
                        break;
                }
            }
            else {
                switch (num){
                    case 0: popupImg.setImageResource(R.drawable.img_cat_house_01);
                        break;
                    case 1: popupImg.setImageResource(R.drawable.img_cat_house_02);
                        break;
                    case 2: popupImg.setImageResource(R.drawable.img_cat_house_03);
                        break;
                }
            }
        }

    }

    public void setBtn(int checkDogCat, int btnValue,int num) {
        switch(checkDogCat){
            case 0:
                button.setOnClickListener(view -> {
                String url = dogUrls[btnValue][num];
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            });
                break;
            case 1:
                button.setOnClickListener(view -> {
                    String url = catUrls[btnValue][num];
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                });
                break;
        }

    }
}

