package code.uz.entity;


import code.uz.enums.GeneralStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profile")
@Getter
@Setter
public class ProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // id kurinmagani yaxshi shunday bulib
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "username")
    private String username; // phone/email
    @Column(name = "updated_username")
    private String updatedUsername;
    @Column(name = "password")
    private String password;
    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private GeneralStatus status;
    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;

    @Column(name = "photo_id")
    private String photoId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", updatable = false, insertable = false)
    private AttachEntity photo;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY) // bunday qilsak console da query kam ketadi (performance ga yaxshi)
    private List<ProfileRoleEntity> role;
}
