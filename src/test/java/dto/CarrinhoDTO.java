package dto;

import java.util.List;

public class CarrinhoDTO {
    private String idProduto;
    private Integer quantidade;

    public CarrinhoDTO() {
    }

    public CarrinhoDTO(String idProduto, Integer quantidade) {
        this.idProduto = idProduto;
        this.quantidade = quantidade;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}

