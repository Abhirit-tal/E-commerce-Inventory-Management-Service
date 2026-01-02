package com.example.inventory.controller;

import com.example.inventory.dto.inventory.InventoryAdjustmentDTO;
import com.example.inventory.service.InventoryAdjustmentService;
import com.example.inventory.service.SkuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryAdjustmentController.class)
public class InventoryAdjustmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryAdjustmentService service;

    @MockBean
    private SkuService skuService;

    @Test
    public void listBySku_returnsPage() throws Exception {
        InventoryAdjustmentDTO dto = new InventoryAdjustmentDTO();
        dto.setId(1L);
        dto.setSkuId(10L);
        dto.setDelta(3);

        when(service.listBySku(any(Long.class), any())).thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/inventory-adjustments/sku/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
}
