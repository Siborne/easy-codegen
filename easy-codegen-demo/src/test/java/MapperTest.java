import org.flyingsparrow.RunDemoApplication;
import org.flyingsparrow.entity.po.ProductInfo;
import org.flyingsparrow.entity.query.ProductInfoQuery;
import org.flyingsparrow.mappers.ProductInfoMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RunDemoApplication.class)
public class MapperTest {

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;
    @Test
    public void selectList() {
        ProductInfoQuery query = new ProductInfoQuery();
//        query.setId(9);
//        query.setCreateTimeStart("2022-08-27");
        query.setCodeFuzzy("5");
        List<ProductInfo> list = productInfoMapper.selectList(query);
        for (ProductInfo productInfo : list){
            System.out.println(productInfo);
        }
        Integer count = productInfoMapper.selectCount(query);
        System.out.println(count);
    }

    @Test
    public void insert() {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setCode("10006");
        productInfo.setSkuType(6);
        productInfo.setColorType(0);
        productInfo.setCreateTime(new Date());
        productInfo.setCreateDate(new Date());
        Integer  res = productInfoMapper.insert(productInfo);
        System.out.println(productInfo.getId());
        System.out.println(res);
    }

    @Test
    public void insertOrUpdate() {
        ProductInfo productInfo = new ProductInfo();
//        productInfo.setId(9);
//        productInfo.setCode("10006");
//        productInfo.setSkuType(6);
//        productInfo.setColorType(0);
//        productInfo.setCreateTime(new Date());
//        productInfo.setCreateDate(new Date());
        productInfo.setId(10);
        productInfo.setCode("10007");
        productInfo.setProductName("测试5");
        Integer  res = productInfoMapper.insertOrUpdate(productInfo);
        System.out.println(productInfo.getId());
        System.out.println(res);
    }

    @Test
    public void insertBatch(){
        List<ProductInfo> productInfoList = new ArrayList<>();
        ProductInfo productInfo = new ProductInfo();
        productInfo.setCreateTime(new Date());
        productInfo.setCreateDate(new Date());
        productInfo.setCode("00000002");
        productInfoList.add(productInfo);

        productInfo = new ProductInfo();
        productInfo.setCreateTime(new Date());
        productInfo.setCreateDate(new Date());
        productInfo.setCode("00000003");
        productInfoList.add(productInfo);

        productInfoMapper.insertBatch(productInfoList);
    }

    @Test
    public void insertBatchOrUpdateBatch(){
        List<ProductInfo> productInfoList = new ArrayList<>();
        ProductInfo productInfo = new ProductInfo();
        productInfo.setCreateTime(new Date());
        productInfo.setCreateDate(new Date());
        productInfo.setCode("00000002");
        productInfo.setProductName("name002");
        productInfoList.add(productInfo);

        productInfo = new ProductInfo();
        productInfo.setCreateTime(new Date());
        productInfo.setCreateDate(new Date());
        productInfo.setCode("00000003");
        productInfo.setProductName("name003");
        productInfoList.add(productInfo);

        productInfoMapper.insertOrUpdateBatch(productInfoList);
    }

    @Test
    public void selectByKey(){
        ProductInfo productInfo1 = productInfoMapper.selectById(9);
        ProductInfo productInfo2 = productInfoMapper.selectByCode("10006");
        ProductInfo productInfo3 = productInfoMapper.selectBySkuTypeAndColorType(5, 3);
        System.out.println(productInfo1);
        System.out.println(productInfo2);
        System.out.println(productInfo3);
    }

    @Test
    public void updateByKey(){
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductName("update by 9");
        productInfoMapper.updateById(productInfo, 9);

        productInfo = new ProductInfo();
        productInfo.setProductName("update by code");
        productInfoMapper.updateById(productInfo,10);

        productInfo = new ProductInfo();
        productInfo.setProductName("update by skuType and colorType");
        productInfoMapper.updateBySkuTypeAndColorType(productInfo, 3, 1);
    }

    @Test
    public void deleteByKey(){
        productInfoMapper.deleteById(22);
        productInfoMapper.deleteByCode("00000001");
        productInfoMapper.deleteBySkuTypeAndColorType(3, 1);
    }

}
