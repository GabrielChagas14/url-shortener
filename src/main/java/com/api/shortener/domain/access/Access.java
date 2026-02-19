package com.api.shortener.domain.access;

import com.api.shortener.domain.url.Url;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name="access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Access {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userAgent;
    private Date accessDate;
    private String zone;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;

}
