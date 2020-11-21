package com.example.cam4pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

    String[][] food = {
            {"The Real 더리얼 그레이프리 크런치 닭고기", "ANF 산전연령 유기농 6Free 연어 애견사료", "에코1 유기농 양고기 애견사료"},
            {"53990won", "40000won", "19000won"},
            {"하림","대산앤컴퍼니","네츄럴코어"},
            {"닭고기, 병아리콩, 타피오카전분..","연어, 닭고기, 쌀, 현미..","양고기, 시금치, 고구마 ..."}
    };
    String [][] snack = {
            {"밀크껌 대", "부드러운 덴탈껌 강아지 치석제거", "강아지 트릿"},
            {"9900won", "9800won", "23700won"},
            {"굿프랜드", "피피픽", "반려소반"},
            {"우피, 우유향","쌀, 타피오카 전분, 콜라겟, 치킨펫..","닭가슴살 100%"}
    };

    String [][] toy = {
                    {"라텍스 스포츠 반려견 장난감", "딩동펫 반려동물 터그장난감 시바", "딩동펫 반려견 공룡알 간식볼"},
                    {"5,720won", "8,800won", "8,800won"},
                    {"제제펫", "딩동펫", "딩동펫"},
                    {"라텍스..","폴라폴리스, 솜..","PP.."}
    };

    String [][] urls = {
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

        setName(btnValue, num);
        setImg(btnValue, num);
        setBtn(btnValue,num);

        ok_button.setOnClickListener(view -> {
            finish();
        });
    }

    public void setName(int btnValue, int num){
        switch (btnValue){
            case 0:
                productName.setText("상품명: "+ food[0][num]);
                productPrice.setText("가격: " + food[1][num]);
                productCompany.setText("회사명: " + food[2][num]);
                productIngre.setText("원재료: "+ food[3][num]);
                break;
            case 1:
                productName.setText("상품명: "+ snack[0][num]);
                productPrice.setText("가격: " + snack[1][num]);
                productCompany.setText("회사명: " + snack[2][num]);
                productIngre.setText("원재료: "+ snack[3][num]);
                break;
            case 2:
                productName.setText("상품명: "+ toy[0][num]);
                productPrice.setText("가격: " + toy[1][num]);
                productCompany.setText("회사명: " + toy[2][num]);
                productIngre.setText("원재료: "+ toy[3][num]);
                break;
        }
    }

    public void setImg(int btnValue, int num){
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

    public void setBtn(int btnValue,int num) {
        button.setOnClickListener(view -> {
            switch (btnValue) {
                case 0:
                    String url = urls[btnValue][num];
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
            }
        });
    }
}

