package com.kusitms.mainservice.domain.template.service;


import com.kusitms.mainservice.domain.roadmap.domain.RoadmapTemplate;
import com.kusitms.mainservice.domain.roadmap.dto.response.RoadmapTitleResponseDto;
import com.kusitms.mainservice.domain.roadmap.repository.RoadmapRepository;
import com.kusitms.mainservice.domain.team.domain.Team;
import com.kusitms.mainservice.domain.team.dto.response.TeamTitleResponseDto;
import com.kusitms.mainservice.domain.team.repository.TeamRepository;
import com.kusitms.mainservice.domain.template.domain.*;
import com.kusitms.mainservice.domain.template.dto.response.*;
import com.kusitms.mainservice.domain.template.repository.*;

import com.kusitms.mainservice.domain.user.domain.User;
import com.kusitms.mainservice.domain.user.dto.response.DetailUserResponseDto;
import com.kusitms.mainservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TemplateService {
    private final TemplateRepository templateRepository;
    private final TemplateContentRepository templateContentRepository;
    private final ReviewerRepository reviewerRepository;
    private final TemplateDownloadRepository templateDownloadRepository;
    private final RoadmapRepository roadmapRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final CustomTemplateRepository customTemplateRepository;
    public SearchTemplateResponseDto searchTemplatesByCategory(TemplateType templateType) {
        if(TemplateType.ALL.equals(templateType)) {
            List<Template> templateList = templateRepository.findAll();
            List<SearchBaseTemplateResponseDto> searchbaseTemplateResponseDtoList = createSearchBaseTemplateResponseDtoList(templateList);
            return SearchTemplateResponseDto.of(searchbaseTemplateResponseDtoList);
        }
        else {
        List<Template> templateList = getTemplateFromTemplateType(templateType);
        List<SearchBaseTemplateResponseDto> searchbaseTemplateResponseDtoList = createSearchBaseTemplateResponseDtoList(templateList);
        return SearchTemplateResponseDto.of(searchbaseTemplateResponseDtoList);
        }
    }
    public SearchTemplateResponseDto searchTemplatesByTitle(String title) {
        List<Template> templateList = getTemplateByTitle(title);
        List<SearchBaseTemplateResponseDto> searchbaseTemplateResponseDtoList= createSearchBaseTemplateResponseDtoList(templateList);
        return SearchTemplateResponseDto.of(searchbaseTemplateResponseDtoList);
    }
    public TemplateDetailResponseDto getTemplateDetail(Long templateId){
        Optional<Template> template = templateRepository.findById(templateId);
        Optional<TemplateContent> templateContent = templateContentRepository.findById(templateId.toString());
        List<Template> templateList = getTemplatesBySameCategoryAndId(template.get().getTemplateType(),template);
        List<RoadmapTitleResponseDto> roadmapTitleResponseDto = getRoadmapTitleResponseDto(template);
        List<SearchBaseTemplateResponseDto> relatedTemplate = createSearchBaseTemplateResponseDtoList(templateList);
        RatingResponseDto ratingResponseDto = createRatingResponse(template);
        int teamCount = getTeamCount(template);
        List<ReviewContentResponseDto> reviewContentResponseDtoList = createReviewContentResponseDto(template);
        DetailUserResponseDto detailUserResponseDto =createTemplateDetailUserResponseDto(templateId);
        String date = template.get().getDate();
        return TemplateDetailResponseDto.of(template, templateContent,roadmapTitleResponseDto,SearchTemplateResponseDto.of(relatedTemplate),ratingResponseDto, teamCount,reviewContentResponseDtoList, detailUserResponseDto, date);
    }

    public GetTeamForSaveTemplateResponseDto getTeamForSaveTemplateByUserId(Long id){
        List<Team> teams = teamRepository.findAllByUserId(id);
        List<TeamTitleResponseDto> teamTitleResponseDtoList = new ArrayList<>();
        for(Team team : teams){
            List<TeamTitleResponseDto> titles = team.getTeamSpaceList()
                    .stream()
                    .map(teamSpace -> TeamTitleResponseDto.of(teamSpace.getTeam().getTitle()))
                    .collect(Collectors.toList());
            teamTitleResponseDtoList.addAll(titles);
        }
        //commit
        return GetTeamForSaveTemplateResponseDto.of(teamTitleResponseDtoList);
    }
    public String saveTemplateByUserId(SaveTemplateResponseDto saveTemplateResponseDto){
        Optional<Template> template = templateRepository.findById(saveTemplateResponseDto.getTemplateid());
        Optional<User> user = userRepository.findById(saveTemplateResponseDto.getUserid());
        TemplateDownload templateDownload = TemplateDownload.createTemplateDownload(user.get(),template.get());
        templateDownloadRepository.save(templateDownload);
        return "저장";

    }
    private List<Template> getTemplateFromTemplateType(TemplateType templateType) {
        return templateRepository.findAllByTemplateType(templateType);
    }
    private List<SearchBaseTemplateResponseDto> createSearchBaseTemplateResponseDtoList(List<Template> templateList) {
        return templateList.stream()
                .map(template ->
                        SearchBaseTemplateResponseDto.of(
                                template,
                                getTeamCount(Optional.of(template)),
                                getRoadmapTitleResponseDto(Optional.of(template))))
                .collect(Collectors.toList());
    }
    private List<RoadmapTitleResponseDto> getRoadmapTitleResponseDto(Optional<Template> template) {

        List<RoadmapTitleResponseDto> roadmapTitleResponseDtoList = new ArrayList<>();

        List<RoadmapTemplate> roadmapTemplates = template.get().getRoadmapTemplates();
        for (RoadmapTemplate roadmapTemplate : roadmapTemplates) {
            String title = roadmapTemplate.getRoadmapSpace().getRoadmap().getTitle();
            RoadmapTitleResponseDto titleResponseDto = RoadmapTitleResponseDto.of(title);
            roadmapTitleResponseDtoList.add(titleResponseDto);


        }
        return roadmapTitleResponseDtoList;
    }
    private List<Template> getTemplateByTitle(String title) {
        return templateRepository.findByTitleContaining(title);
    }
    private RatingResponseDto createRatingResponse(Optional<Template> template) {
        List<Reviewer> reviewers = reviewerRepository.findByTemplate(template);
        Double totalRating = 0.0;
        int numRatings = 0;

        for (Reviewer reviewer : reviewers) {
            if (reviewer.getTemplateReview() != null) {
                totalRating = totalRating + reviewer.getTemplateReview().getRating();
                numRatings++;
            }
        }

        if (numRatings > 0) {
            return RatingResponseDto.of(totalRating / numRatings);
        } else {
            return RatingResponseDto.of(0.0);
        }
    }
    private int getTeamCount(Optional<Template> template){
        int tc = templateDownloadRepository.countDownloadsByTemplate(template);
        return tc;
    }
    private List<ReviewContentResponseDto> createReviewContentResponseDto(Optional<Template> template){
        List<Reviewer> reviewers = reviewerRepository.findByTemplate(template);
        List<ReviewContentResponseDto> reviewContentResponseDtoList = new ArrayList<>();
        for (Reviewer reviewer : reviewers) {
            if (reviewer.getTemplateReview() != null) {
                ReviewContentResponseDto reviewContentResponseDto=ReviewContentResponseDto.of(reviewer.getTemplateReview().getContent());
                reviewContentResponseDtoList.add(reviewContentResponseDto);
            }
        }
        return reviewContentResponseDtoList;
    }
    private DetailUserResponseDto createTemplateDetailUserResponseDto(Long templateId){
        User user = templateRepository.findUserById(templateId);
        int templateNum = getTemplateCountByUser(user);
        int roadmapNum = getRoadmapCountByUser(user);
        return DetailUserResponseDto.of(user, templateNum,roadmapNum);
    }
    private int getTemplateCountByUser(User user) {
        return templateRepository.countByUser(user);
    }
    private int getRoadmapCountByUser(User user) {
        return roadmapRepository.countByUser(user);
    }
    private List<Template> getTemplatesBySameCategoryAndId(TemplateType templateType, Optional<Template> template){
        List<Template> templates = templateRepository.findAllByTemplateType(templateType);
        template.ifPresent(t -> templates.removeIf(existingTemplate -> existingTemplate.getId().equals(t.getId())));
        return templates;
    }
}
