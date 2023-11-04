package com.kusitms.mainservice.domain.roadmap.domain;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "custom_roadmap")
@Entity
public class CustomRoadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_roadmap_id")
    private Long id;
    private String title;
    private String goal;
    @Enumerated(value = EnumType.STRING)
    private RoadmapType roadmapType;
    @OneToOne
    private RoadmapDownload roadmapDownload;
}