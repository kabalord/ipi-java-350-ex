package com.ipiecoles.java.java350.service;

import com.ipiecoles.java.java350.exception.EmployeException;
import com.ipiecoles.java.java350.model.Employe;
import com.ipiecoles.java.java350.model.Entreprise;
import com.ipiecoles.java.java350.model.NiveauEtude;
import com.ipiecoles.java.java350.model.Poste;
import com.ipiecoles.java.java350.repository.EmployeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeServiceTest {

    @InjectMocks
    private EmployeService employeService;

    @Mock
    private EmployeRepository employeRepository;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this.getClass());
        employeRepository.deleteAll();
    }

    @Test
    void testEmbaucheEmployeTechnicienPleinTempsBts() throws EmployeException {

        //Given

        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = 1.0;
        Mockito.when(employeRepository.findLastMatricule()).thenReturn(null);
        Mockito.when(employeRepository.findByMatricule("T00001")).thenReturn(null);
        Mockito.when(employeRepository.save(Mockito.any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        //When
        Employe e = employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        //Si  employé existe dans la base avec les bonnes infos
        Assertions.assertEquals("T00001", e.getMatricule());
        Assertions.assertEquals(nom, e.getNom());
        Assertions.assertEquals(prenom, e.getPrenom());
        Assertions.assertEquals(LocalDate.now(), e.getDateEmbauche());
        Assertions.assertEquals(Entreprise.PERFORMANCE_BASE, e.getPerformance());
        //152122.22 * 1.2 * 1.0 = 1825.46
        Assertions.assertEquals(Entreprise.PERFORMANCE_BASE, e.getPerformance());
        Assertions.assertEquals(1825.46, (double)e.getSalaire());
        Assertions.assertEquals(tempsPartiel, e.getTempsPartiel());


    }

    @Test
    public void testEmbaucheEmployeManagerMiTempsMasterLastMatricule00345() throws EmployeException {

        //Given
        String nom = "Diana";
        String prenom = "Benitez";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        Mockito.when(employeRepository.findLastMatricule()).thenReturn("00345");
        Mockito.when((employeRepository.findByMatricule("M00346"))).thenReturn(null);
        Mockito.when(employeRepository.save(Mockito.any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        //When
        Employe e = employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        Assertions.assertEquals("M00346",e.getMatricule());
        Assertions.assertEquals(nom,e.getNom());
        Assertions.assertEquals(prenom, e.getPrenom());
        Assertions.assertEquals(LocalDate.now(), e.getDateEmbauche());
        Assertions.assertEquals(Entreprise.PERFORMANCE_BASE, e.getPerformance());
        Assertions.assertEquals(1064.85, (double)e.getSalaire());
        Assertions.assertEquals(tempsPartiel, e.getTempsPartiel());
    }

    @Test
    public void testEmbaucheEmployeManagerMiTempsMaster99999(){
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        Mockito.when(employeRepository.findLastMatricule()).thenReturn("99999");

        //When/Then
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        Assertions.assertEquals("Limite des 100000 matricules atteinte !", e.getMessage());
    }

    @Test
    public void testEmbaucheEmployeManagerMitempsMasterExistingEmploye() /*throws EmployeException*/ {
        //Given
        String nom = "Benitez";
        String prenom = "Diana";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        Mockito.when(employeRepository.findLastMatricule()).thenReturn(null);
        Mockito.when(employeRepository.findByMatricule("M00001")).thenReturn(new Employe());

        //When
        EntityExistsException e = Assertions.assertThrows(EntityExistsException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        Assertions.assertEquals("L'employé de matricule M00001 existe déjà en BDD", e.getMessage());

        //ou avec catch
        /*try {
            employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);
            Assertions.fail("Aurait du planter");
        }catch (EntityExistsException e){
            Assertions.assertEquals("L'employé de matricule M00001 existe déjà en BDD", e.getMessage());
        }*/
    }

    @Test
    public void testCalculPerformanceCommercialCaTraiteNull() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = null;
        Long objectifCa = 2000L;

        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));

        //Then
        Assertions.assertEquals("Le chiffre d'affaire traité ne peut être négatif ou null !", e.getMessage());
    }

    @Test
    public void testCalculPerformanceCommercialCaTraiteNegative() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = -100L;
        Long objectifCa = 2000L;

        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));

        //Then
        Assertions.assertEquals("Le chiffre d'affaire traité ne peut être négatif ou null !", e.getMessage());
    }


    @Test
    public void testCalculPerformanceCommercialMatriculeDifferente() throws EmployeException {

        //Given
        String matricule = "M00001";
        Long caTraite = 2000L;
        Long objectifCa = 2000L;

        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));

        //Then
        Assertions.assertEquals("Le matricule ne peut être null et doit commencer par un C !", e.getMessage());
    }

    @Test
    public void testCalculPerformanceCommercialMatriculeNull() throws EmployeException {
        //Given
        String matricule = null;
        Long caTraite = 2000L;
        Long objectifCa = 2000L;

        //When//Then
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));
        Assertions.assertEquals("Le matricule ne peut être null et doit commencer par un C !", e.getMessage());
    }

    @Test
    public void testCalculPerformanceCommercialObjectifCaNull() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2000L;
        Long objectifCa = null;

        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));

        //Then
        Assertions.assertEquals("L'objectif de chiffre d'affaire ne peut être négatif ou null !", e.getMessage());
    }

    @Test
    public void testCalculPerformanceCommercialObjectifCaNegative() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2000L;
        Long objectifCa = -100L;

        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));

        //Then
        Assertions.assertEquals("L'objectif de chiffre d'affaire ne peut être négatif ou null !", e.getMessage());
    }

    @Test
    public void testCalculPerformanceCommercialEmployeNull() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2000L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(null);

        //When
        EmployeException e = Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));

        //Then
        Assertions.assertEquals("Le matricule " + matricule + " n'existe pas !", e.getMessage());
    }

    @Test
    public void testCalculPerformanceCommercialCasDeux() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 1800L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1.0);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(1, (int)employeArgumentCaptor.getValue().getPerformance());
    }

    @Test
    public void testCalculPerformanceCommercialCasTrois() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2000L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1.0);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(1, (int)employeArgumentCaptor.getValue().getPerformance());
    }

    @Test
    public void testCalculPerformanceCommercialCasQuatreMoyenSuperior() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2200L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(5.0);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(2, (int)employeArgumentCaptor.getValue().getPerformance());
    }

    @Test
    public void testCalculPerformanceCommercialCasQuatreMoyenInferiur() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2200L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1.0);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(3, (int)employeArgumentCaptor.getValue().getPerformance());
    }

    @Test
    public void testCalculPerformanceCommercialCasCinqMoyenSuperior() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2500L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(10.0);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(5, (int)employeArgumentCaptor.getValue().getPerformance());
    }

    @Test
    public void testCalculPerformanceCommercialCasCinqMoyenInferiur() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 2500L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1.0);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(6, (int)employeArgumentCaptor.getValue().getPerformance());
    }

    @Test
    public void testCalculPerformanceCommercialCaseNominal() throws EmployeException {

        //Given
        String matricule = "C00001";
        Long caTraite = 1000L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(1.0);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(1, (int)employeArgumentCaptor.getValue().getPerformance());
    }

    @Test
    public void testCalculPerformanceCommercialCaseNull() throws EmployeException {
        //Given
        String matricule = "C00001";
        Long caTraite = 1000L;
        Long objectifCa = 2000L;
        when(employeRepository.findByMatricule("C00001")).thenReturn(new Employe());
        when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(null);

        //When
        employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(1, (int)employeArgumentCaptor.getValue().getPerformance());
    }


    @Test
    public void testEmbaucheEmployeTechnicienBtsTempsNull() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = null;
        when(employeRepository.findLastMatricule()).thenReturn("00345");
        when(employeRepository.findByMatricule("T00346")).thenReturn(null);

        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository, times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertEquals(nom, employeArgumentCaptor.getValue().getNom());
        Assertions.assertEquals(prenom, employeArgumentCaptor.getValue().getPrenom());
        Assertions.assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), employeArgumentCaptor.getValue().getDateEmbauche().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        Assertions.assertEquals("T00346", employeArgumentCaptor.getValue().getMatricule());
        Assertions.assertEquals(tempsPartiel, employeArgumentCaptor.getValue().getTempsPartiel());
        Assertions.assertEquals(1825.46, employeArgumentCaptor.getValue().getSalaire().doubleValue());
    }

}
