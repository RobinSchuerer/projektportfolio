package de.lv1871.projektportfolio.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import de.lv1871.projektportfolio.domain.*;
import de.lv1871.projektportfolio.reader.ProjektPortfolioExcelReader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GleichverteilungMitFolgeCheckTest {

    private static final Projekt MUSS_PROJEKT = Projekt
            .newBuilder()
            .withName("Test1")
            .withTyp(ProjektTyp.MUSS_PROJEKT)
            .withDeadLine(LocalDate.parse("2016-12-01"))
            .build();

    private static final Projekt MUSS_PROJEKT2 = Projekt
            .newBuilder()
            .withName("Test3")
            .withTyp(ProjektTyp.MUSS_PROJEKT)
            .withDeadLine(LocalDate.parse("2016-12-01"))
            .build();


    private static final Projekt PRODUKT_PROJEKT = Projekt
            .newBuilder()
            .withName("Test2")
            .withTyp(ProjektTyp.PRODUKT_PROJEKT)
            .withDeadLine(LocalDate.parse("2016-12-01"))
            .build();

    private static final Team TEAM = Team.newBuilder().withName("team").build();

    private GleichverteilungMitFolgeCheck toTest = new GleichverteilungMitFolgeCheck();

    @Test
    public void fall01() {
//        Anderes Beispiel:
//        80% Muss Projekte
//        30% Produkt Projekte
//        10 PT
//
//                ==> 5 PT Muss und 5 PT Produkt verletzt die 30%-Bedingung bei Produkt-Projekten
//                ==> nur 3 PT für das Produktprojekt und 7 PT für das Muss-Projekt
//                 (==> beide Bedingungen erfüllt)


        ProjektPortfolioEingabeDaten eingabeDaten = ProjektPortfolioEingabeDaten
                .newBuilder()
                .withProjektAufwaende(Lists.newArrayList(
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(MUSS_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build(),
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(PRODUKT_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build()))
                .withTeamKapazitaeten(Lists.newArrayList(
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-12-01"))
                                .withKapazitaet(BigDecimal.TEN)
                                .build(),
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-11-01"))
                                .withKapazitaet(BigDecimal.TEN)
                                .build()))
                .withBeschraenkungen(Lists.newArrayList(
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.MUSS_PROJEKT)
                                .withValue(new BigDecimal("0.80"))
                                .build(),
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.PRODUKT_PROJEKT)
                                .withValue(new BigDecimal("0.30"))
                                .build()
                ))
                .build();

        ProjektPortfolioVorschlag result = toTest.verarbeite(eingabeDaten, TEAM);
        assertEquals(new BigDecimal("7.00"), getValue(result, TEAM.getName(), MUSS_PROJEKT.getName(), "2016-12-01"));
        assertEquals(new BigDecimal("3.00"), getValue(result, TEAM.getName(), PRODUKT_PROJEKT.getName(), "2016-12-01"));

        assertEquals(new BigDecimal("3.00"), getValue(result, TEAM.getName(), MUSS_PROJEKT.getName(), "2016-11-01"));
        assertEquals(new BigDecimal("3.00"), getValue(result, TEAM.getName(), PRODUKT_PROJEKT.getName(), "2016-11-01"));

    }

    @Test
    public void fall02() {
        //        Weiteres Beispiel
        //        80% Muss
        //        100% Produkt
        //
        //        Mussprj                Prod-Prj
        //        5 PT                5 PT

        ProjektPortfolioEingabeDaten eingabeDaten = ProjektPortfolioEingabeDaten
                .newBuilder()
                .withProjektAufwaende(Lists.newArrayList(
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(MUSS_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build(),
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(PRODUKT_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build()))
                .withTeamKapazitaeten(Lists.newArrayList(
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-12-01"))
                                .withKapazitaet(BigDecimal.TEN)
                                .build(),
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-11-01"))
                                .withKapazitaet(BigDecimal.TEN)
                                .build()))
                .withBeschraenkungen(Lists.newArrayList(
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.MUSS_PROJEKT)
                                .withValue(new BigDecimal("0.80"))
                                .build(),
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.PRODUKT_PROJEKT)
                                .withValue(new BigDecimal("1.00"))
                                .build()
                ))
                .build();

        ProjektPortfolioVorschlag result = toTest.verarbeite(eingabeDaten, TEAM);
        assertEquals(new BigDecimal("5.00"), getValue(result, TEAM.getName(), MUSS_PROJEKT.getName(), "2016-12-01"));
        assertEquals(new BigDecimal("5.00"), getValue(result, TEAM.getName(), PRODUKT_PROJEKT.getName(), "2016-12-01"));

        assertEquals(new BigDecimal("5.00"), getValue(result, TEAM.getName(), MUSS_PROJEKT.getName(), "2016-11-01"));
        assertEquals(new BigDecimal("5.00"), getValue(result, TEAM.getName(), PRODUKT_PROJEKT.getName(), "2016-11-01"));


    }



    @Test
    public void fall03() {
        //        Noch ein Beispiel mit
        //        50% Muss
        //        50% Produkt
        //        9 PT
        //        Aber mit 2 Muss-Projekten und einem Produkt-Projekt
        //
        //        Gleichverteilung
        //        Muss1        Muss2         Prod
        //        3 PT        3 PT         3 PT  ==> Geht nicht
        //==>
        //        2,25 PT        2,25 PT        4,5 PT        erfüllt beide Restriktionen

        ProjektPortfolioEingabeDaten eingabeDaten = ProjektPortfolioEingabeDaten
                .newBuilder()
                .withProjektAufwaende(Lists.newArrayList(
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(MUSS_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build(),
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(MUSS_PROJEKT2)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build(),

                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(PRODUKT_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build()))
                .withTeamKapazitaeten(Lists.newArrayList(
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-12-01"))
                                .withKapazitaet(new BigDecimal("9"))
                                .build(),
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-11-01"))
                                .withKapazitaet(new BigDecimal("9"))
                                .build()))
                .withBeschraenkungen(Lists.newArrayList(
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.MUSS_PROJEKT)
                                .withValue(new BigDecimal("0.50"))
                                .build(),
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.PRODUKT_PROJEKT)
                                .withValue(new BigDecimal("0.50"))
                                .build()
                ))
                .build();

        ProjektPortfolioVorschlag result = toTest.verarbeite(eingabeDaten, TEAM);
        assertEquals(new BigDecimal("2.25"), getValue(result, TEAM.getName(), MUSS_PROJEKT.getName(), "2016-12-01"));
        assertEquals(new BigDecimal("2.25"), getValue(result, TEAM.getName(), MUSS_PROJEKT2.getName(), "2016-12-01"));
        assertEquals(new BigDecimal("4.50"), getValue(result, TEAM.getName(), PRODUKT_PROJEKT.getName(), "2016-12-01"));

        assertEquals(new BigDecimal("2.25"), getValue(result, TEAM.getName(), MUSS_PROJEKT.getName(), "2016-11-01"));
        assertEquals(new BigDecimal("2.25"), getValue(result, TEAM.getName(), MUSS_PROJEKT2.getName(), "2016-11-01"));
        assertEquals(new BigDecimal("4.50"), getValue(result, TEAM.getName(), PRODUKT_PROJEKT.getName(), "2016-11-01"));

    }


    @Test
    public void fall04() {
        //        Variante:
        //        100% Muss
        //        20% Produkt
        //        10 PT
        //        2 Muss-Projekte, 1 Produkt-Projekt
        //                ==>
        //        Muss1        Muss2        Produkt
        //        3,33        3,33        3,33        geht nicht, da Produkt<=20% verletzt ist.
        //                ==>
        //        4 PT        4 PT        2 PT

        ProjektPortfolioEingabeDaten eingabeDaten = ProjektPortfolioEingabeDaten
                .newBuilder()
                .withProjektAufwaende(Lists.newArrayList(
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(MUSS_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build(),
                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(MUSS_PROJEKT2)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build(),

                        ProjektAufwand
                                .newBuilder()
                                .withProjekt(PRODUKT_PROJEKT)
                                .withAufwaende(ImmutableMap.of(TEAM, BigDecimal.TEN))
                                .build()))
                .withTeamKapazitaeten(Lists.newArrayList(
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-12-01"))
                                .withKapazitaet(new BigDecimal("10"))
                                .build(),
                        TeamKapazitaet
                                .newBuilder()
                                .withTeam(TEAM)
                                .withMonat(LocalDate.parse("2016-11-01"))
                                .withKapazitaet(new BigDecimal("10"))
                                .build()))
                .withBeschraenkungen(Lists.newArrayList(
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.MUSS_PROJEKT)
                                .withValue(new BigDecimal("1.00"))
                                .build(),
                        Beschraenkung
                                .newBuilder()
                                .withTyp(ProjektTyp.PRODUKT_PROJEKT)
                                .withValue(new BigDecimal("0.20"))
                                .build()
                ))
                .build();

        ProjektPortfolioVorschlag result = toTest.verarbeite(eingabeDaten, TEAM);
        assertEquals(new BigDecimal("4.00"), getValue(result, TEAM.getName(), MUSS_PROJEKT.getName(), "2016-12-01"));
        assertEquals(new BigDecimal("4.00"), getValue(result, TEAM.getName(), MUSS_PROJEKT2.getName(), "2016-12-01"));
        assertEquals(new BigDecimal("2.00"), getValue(result, TEAM.getName(), PRODUKT_PROJEKT.getName(), "2016-12-01"));
    }

    @Test
    public void zweiMussProjekteMitOverflow() throws Exception {
        ProjektPortfolioExcelReader reader = new ProjektPortfolioExcelReader();
        String path = "/005 One-team-two-mhprojects-with-overflow.xlsx";

        Path resPath = Paths.get(getClass().getResource(path).toURI());
        ProjektPortfolioEingabeDaten eingabeDaten = reader.read(new XSSFWorkbook(Files.newInputStream(resPath)));

        ProjektPortfolioVorschlag vorschlag = toTest.verarbeite(eingabeDaten, Team.newBuilder().withName("Team1").build());

        assertEquals(new BigDecimal("8.00"), getValue(vorschlag,"Team1", "Project1", "2016-12-01"));
        assertEquals(new BigDecimal("8.00"), getValue(vorschlag,"Team1", "Project1", "2016-11-01"));
        assertEquals(new BigDecimal("4.00"), getValue(vorschlag,"Team1", "Project1", "2016-10-01"));
        assertEquals(new BigDecimal("4.00"), getValue(vorschlag,"Team1", "Project1", "2016-09-01"));
        assertEquals(new BigDecimal("2.00"), getValue(vorschlag,"Team1", "Project1", "2016-08-01"));
        assertEquals(new BigDecimal("2.80"), getValue(vorschlag,"Team1", "Project1", "2016-07-01"));
        assertEquals(new BigDecimal("4.00"), getValue(vorschlag,"Team1", "Project1", "2016-06-01"));
        assertEquals(new BigDecimal("2.20"), getValue(vorschlag,"Team1", "Project1", "2016-05-01"));


        assertEquals(new BigDecimal("4.00"), getValue(vorschlag,"Team1", "Project2", "2016-10-01"));
        assertEquals(new BigDecimal("4.00"), getValue(vorschlag,"Team1", "Project2", "2016-09-01"));
        assertEquals(new BigDecimal("2.00"), getValue(vorschlag,"Team1", "Project2", "2016-08-01"));
        assertEquals(new BigDecimal("2.80"), getValue(vorschlag,"Team1", "Project2", "2016-07-01"));
        assertEquals(new BigDecimal("4.00"), getValue(vorschlag,"Team1", "Project2", "2016-06-01"));
        assertEquals(new BigDecimal("5.80"), getValue(vorschlag,"Team1", "Project2", "2016-05-01"));
        assertEquals(new BigDecimal("8.00"), getValue(vorschlag,"Team1", "Project2", "2016-04-01"));

        assertEquals(Optional.empty(), vorschlag.getUeberlauf("Team1","Project1"));
        assertEquals(new BigDecimal("9.40"), vorschlag.getUeberlauf("Team1","Project2").get());

    }


    private BigDecimal getValue(ProjektPortfolioVorschlag result,
                                String teamName,
                                String projektName,
                                String monatString) {
        return result.getAufwand(teamName, projektName, LocalDate.parse(monatString)).get();
    }
}
