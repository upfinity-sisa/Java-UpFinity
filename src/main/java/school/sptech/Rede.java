package school.sptech;

import java.time.LocalDateTime;

public class Rede {
    private Integer idRede;
    private String nomeInterface;
    private Double megabytesRecebidos;
    private LocalDateTime momentoRegistro;
    private Integer fkAtm;

    public Integer getIdRede() {
        return idRede;
    }

    public void setIdRede(Integer idRede) {
        this.idRede = idRede;
    }

    public Integer getFkAtm() {
        return fkAtm;
    }

    public void setFkAtm(Integer fkAtm) {
        this.fkAtm = fkAtm;
    }


    public Double getMegabytesRecebidos() {
        return megabytesRecebidos;
    }

    public void setMegabytesRecebidos(Double megabytesRecebidos) {
        this.megabytesRecebidos = megabytesRecebidos;
    }

    public String getNomeInterface() {
        return nomeInterface;
    }

    public void setNomeInterface(String nomeInterface) {
        this.nomeInterface = nomeInterface;
    }

    public LocalDateTime getMomentoRegistro() {
        return momentoRegistro;
    }

    public void setMomentoRegistro(LocalDateTime momentoRegistro) {
        this.momentoRegistro = momentoRegistro;
    }
}
