package com.example.inventory.controller;

import com.example.inventory.dto.inventory.InventoryAdjustmentCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.service.InventoryAdjustmentService;
import com.example.inventory.service.SkuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryAdjustmentController.class)
public class InventoryAdjustmentCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SkuService skuService;

    @MockBean
    private InventoryAdjustmentService inventoryAdjustmentService;

    @Test
    public void createAdjustment_callsSkuServiceAndReturnsSku() throws Exception {
        InventoryAdjustmentCreateDTO req = new InventoryAdjustmentCreateDTO();
        req.setSkuId(10L);
        req.setDelta(2);
        req.setIdempotencyKey("k1");

        SkuDTO resp = new SkuDTO();
        resp.setId(10L);
        resp.setInventoryCount(12);

        when(skuService.adjustInventory(any(Long.class), any())).thenReturn(resp);

        mockMvc.perform(post("/api/inventory-adjustments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryCount").value(12));
    }
}
