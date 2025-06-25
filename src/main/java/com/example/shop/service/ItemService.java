package com.example.shop.service;

import com.example.shop.dto.ItemFormDto;
import com.example.shop.dto.ItemImgDto;
import com.example.shop.entity.Item;
import com.example.shop.entity.ItemImg;
import com.example.shop.repository.ItemImgRepository;
import com.example.shop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgRepository itemImgRepository;
    private final ItemImgService itemImgService;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        // 상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        // 이미지 등록
        for(MultipartFile multipartFile : itemImgFileList) {
            ItemImg itemImg = new ItemImg();

            itemImg.setItem(item);

            if(itemImgFileList.get(0).equals(multipartFile)) {
                itemImg.setRepimgYn("Y"); // 메인페이지 보여줄 대표이미지
            }else{
                itemImg.setRepimgYn("N"); //
            }
            itemImgService.saveItemImg(itemImg, multipartFile);
        }

        return item.getId();
    }

    /*
        Form화면에 ItemFormDto있는 내용을 화면상 보여주고, 그 내용을 수정하기 위한 것이다.
        ItemFormDto에는 item에 있는 상품 내용과 tem_id에 해당하는
        ItemImg 이미지를 조외해서 ItemFormDto에 담아서 itemForm.html에 전달해야한다.
     */


    /*
        Jpa가 더티체킹(변경감지)을 수행하지 않는다.
        즉, itemId 조회해서 조회한 결과를 영속계층에 저장하고, 그 조회한 데이터를 변경해도 update(save) 금지
     */
    @Transactional(readOnly = true)  //JPA가 더티체킹(변경감지)을 수행하지 않는다.
    public ItemFormDto getItemDtl(Long itemId){

        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);

        List<ItemImgDto> itemImgDtoList = new ArrayList<>();

        // ItemImg -> ItemImgDto 변환해서 List 저장
        for(ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);

            itemImgDtoList.add(itemImgDto);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 데이터가 존재하지 않습니다."));

        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);

        return itemFormDto;
    }
}
