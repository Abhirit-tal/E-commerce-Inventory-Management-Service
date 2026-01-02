package com.example.inventory.controller;

import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.service.SkuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SkuController.class)
public class SkuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SkuService skuService;

    @Test
    public void createSku_returns201() throws Exception {
        SkuCreateDTO req = new SkuCreateDTO();
        req.setCode("TS-001");
        req.setProductId(1L);
        req.setPrice(new BigDecimal("9.99"));
        req.setInventoryCount(10);

        SkuDTO resp = new SkuDTO();
        resp.setId(100L);
        resp.setCode("TS-001");
        resp.setInventoryCount(10);

        when(skuService.create(any(SkuCreateDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/skus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.code").value("TS-001"));
    }

    @Test
    public void adjustInventory_returns200() throws Exception {
        InventoryAdjustDTO adj = new InventoryAdjustDTO();
        adj.setDelta(5);

        SkuDTO resp = new SkuDTO();
        resp.setId(100L);
        resp.setInventoryCount(15);

        when(skuService.adjustInventory(eq(100L), any(InventoryAdjustDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/skus/100/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adj)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryCount").value(15));
    }
}

