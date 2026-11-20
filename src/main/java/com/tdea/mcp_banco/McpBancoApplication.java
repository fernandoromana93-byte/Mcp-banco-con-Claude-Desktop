package com.tdea.mcp_banco;

import java.util.List;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.tdea.mcp_banco.tools.BancoTools;

@SpringBootApplication
public class McpBancoApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpBancoApplication.class, args);
    }

    /** Registra los métodos @Tool de BancoTools como herramientas del servidor MCP. */
    @Bean
    public List<ToolCallback> tools(BancoTools bancoTools) {
        return List.of(ToolCallbacks.from(bancoTools));
    }
}
