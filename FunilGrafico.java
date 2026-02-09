/**
 * SISTEMA DE ANÁLISE MORFOLÓGICA E REGRAS DE ACENTUAÇÃO (PT-BR)
 *
 * FINALIDADE:
 *
 * O programa 'FunilGrafico' identifica a classificação tônica de palavras
 * (Oxítonas, Paroxítonas, Proparoxítonas e Monossílabos) e valida se,
 * conforme as normas da Língua Portuguesa, aquela palavra deve ser acentuada.
 *
 * FUNCIONAMENTO:
 *
 * 1. Entrada: Recebe palavras separadas por hífen, onde a sílaba tônica
 * deve estar em CAIXA ALTA (ex: sa-Ú-de, PAS-sa-ro, ca-FÉ).
 *
 * 2. Processamento:
 * - Remove acentos gráficos para análise "limpa".
 * - Identifica a posição da sílaba tônica.
 * - Aplica filtros (funil) baseados em terminações específicas:
 * - Regras de Proparoxítonas (todas).
 * - Hiatos tônicos (I e U).
 * - Ditongos abertos em oxítonas.
 * - Terminações de oxítonas, paroxítonas e monossílabos.
 *
 * 3. Saída: Informa a regra de acentuação aplicada ou se a palavra não é acentuada.
 */

import java.text.Normalizer;
import java.util.Scanner;

public class FunilGrafico {

    enum Classificacao {
        MONOSSILABO_TONICO, OXITONA, PAROXITONA, PROPAROXITONA, NAO_IDENTIFICADA
    }

    record DetalheHiato(char vogal1, char vogal2, int posicaoDaTonica) {
        @Override
        public String toString() {
            String ordem = (posicaoDaTonica == 1) ? "Primeira" : "Segunda";
            return String.format("Encontro: %c-%c | Tônica: %s vogal", vogal1, vogal2, ordem);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\nDigite a palavra (ex: sa-Ú-de, ba-Ú, PAS-sa-ro) ou FIM:");
            String entrada = sc.nextLine();
            if (entrada.equalsIgnoreCase("FIM")) break;

            entrada = removerAcentos(entrada);

            String[] silabas = entrada.split("-");
            int indiceTonica = encontrarIndiceTonica(silabas);

            Classificacao classe = classificarPalavra(silabas, indiceTonica);
            DetalheHiato hiato = encontrarHiatoTonico(silabas, indiceTonica);

            if (Classificacao.PROPAROXITONA.equals(classe)) {
                System.out.println("TÔNICA ACENTUADA: Proparossítona");

            } else if (isHiatoTonicoValido(hiato, silabas, indiceTonica, classe)) {
                System.out.println("TÔNICA ACENTUADA: Hiato I/U tônico");

            } else if (isOxitonaComDitongoAberto(silabas, indiceTonica, classe)) {
                System.out.println("TÔNICA ACENTUADA: Oxítona com ditongo aberto (ÉI, ÓI, ÉU)");

            } else if (isMonosilaboTonicoValido(silabas, classe)) {
                System.out.println("TÔNICA ACENTUADA: Monossílabo tônico terminado em a(s), e(s), o(s)");

            } else if (isOxitonaValida(silabas, classe)) {
                System.out.println("TÔNICA ACENTUADA: Oxítona terminada em a(s), e(s), o(s), em(ens)");

            } else if (isParoxitonaValida(silabas, classe)) {
                System.out.println("TÔNICA ACENTUADA: Paroxítona NÃO TERMINADA em a(s), e(s), o(s), em(ens)");

            } else if (isParoxitonaTerminadaEmDitongo(silabas, indiceTonica, classe)) {
                System.out.println("TÔNICA ACENTUADA: Paroxitona terminada em ditongo");

            } else {
                System.out.println("NÃO É uma palavra acentuada!");
            }
        }

        sc.close();
    }

    private static boolean isOxitonaComDitongoAberto(String[] silabas, int indiceTonica, Classificacao classe) {
        if (indiceTonica != 0 || !hasDitongo(silabas, indiceTonica)) return false;
        String busca = silabas[indiceTonica].toLowerCase();
        return busca.matches(".*(ei|oi|eu).*");
    }

    private static boolean isParoxitonaTerminadaEmDitongo(String[] silabas, int indiceTonica, Classificacao classe) {
        return Classificacao.PAROXITONA.equals(classe) && hasDitongo(silabas, silabas.length - 1);
    }

    private static boolean isOxitonaValida(String[] silabas, Classificacao classe) {
        if (!Classificacao.OXITONA.equals(classe)) return false;

        String ultimaSilaba = silabas[silabas.length - 1].toLowerCase();

        // Padrão: termina com a(s), e(s), o(s), em ou ens
        // O $ garante que a correspondência seja no final da string
        return ultimaSilaba.matches(".*(a|as|e|es|o|os|em|ens)$");
    }

    private static boolean isParoxitonaValida(String[] silabas, Classificacao classe) {
        if (!Classificacao.PAROXITONA.equals(classe)) return false;

        String ultimaSilaba = silabas[silabas.length - 1].toLowerCase();

        // Padrão: termina com a(s), e(s), o(s), em ou ens
        // O $ garante que a correspondência seja no final da string
        return !ultimaSilaba.matches(".*(a|as|e|es|o|os|em|ens)$");
    }

    private static boolean isMonosilaboTonicoValido(String[] silabas, Classificacao classe) {
        if (!Classificacao.MONOSSILABO_TONICO.equals(classe)) return false;

        String ultimaSilaba = silabas[silabas.length - 1];

        // Padrão: termina com a(s), e(s), o(s)
        // O $ garante que a correspondência seja no final da string
        return ultimaSilaba.matches("(?i).*(a|e|o)s?$");
    }

    private static boolean isHiatoTonicoValido(DetalheHiato hiato, String[] silabas,
                                               int indiceTonica, Classificacao classe) {
        if (hiato == null || hiato.posicaoDaTonica != 2) return false;

        // Hiato tônico I/U
        char v = Character.toLowerCase(hiato.vogal2);
        if (v != 'i' && v != 'u') return false;

        // Verifica se a sílaba da tônica tem apenas a vogal ou vogal + s
        String silabaTonica = silabas[indiceTonica].toLowerCase();
        if (!silabaTonica.matches("^[iu]s?$")) return false;

        // Falha se houver 'NH' após o hiato (ex: ra-i-nha)
        if (indiceTonica + 1 < silabas.length) {
            if (silabas[indiceTonica + 1].toLowerCase().contains("nh")) return false;
        }

        // Falha se paroxítona e tônicos I/U após ditongo (fei-u-ra
        if (Classificacao.PAROXITONA.equals(classe) && indiceTonica > 0) {
            if (hasDitongo(silabas, indiceTonica - 1)) return false;
        }

        return true;
    }

    private static Classificacao classificarPalavra(String[] silabas, int indiceTonica) {
        if (indiceTonica == -1) return Classificacao.NAO_IDENTIFICADA;

        int posicaoDeTrasParaFrente = silabas.length - 1 - indiceTonica;

        return switch (posicaoDeTrasParaFrente) {
            case 0 -> (silabas.length == 1) ? Classificacao.MONOSSILABO_TONICO : Classificacao.OXITONA;
            case 1 -> Classificacao.PAROXITONA;
            case 2 -> Classificacao.PROPAROXITONA;
            default -> Classificacao.NAO_IDENTIFICADA;
        };
    }

    private static DetalheHiato encontrarHiatoTonico(String[] silabas, int indiceTonica) {
        if (indiceTonica == -1) return null;

        // Caso 1: A tônica é a SEGUNDA vogal do hiato (Ex: sa-Ú-de)
        if (indiceTonica > 0) {
            String anterior = silabas[indiceTonica - 1];
            String atual = silabas[indiceTonica];

            char ultimaAnterior = anterior.charAt(anterior.length() - 1);
            char primeiraTonica = atual.charAt(0);

            if (isVogal(ultimaAnterior) && isVogal(primeiraTonica)) {
                return new DetalheHiato(ultimaAnterior, primeiraTonica, 2);
            }
        }

        // Caso 2: A tônica é a PRIMEIRA vogal do hiato (Ex: SÁ-a-ra, VO-o)
        if (indiceTonica < silabas.length - 1) {
            String atual = silabas[indiceTonica];
            String proxima = silabas[indiceTonica + 1];

            char ultimaTonica = atual.charAt(atual.length() - 1);
            char primeiraProxima = proxima.charAt(0);

            if (isVogal(ultimaTonica) && isVogal(primeiraProxima)) {
                return new DetalheHiato(ultimaTonica, primeiraProxima, 1);
            }
        }

        return null;
    }

    private static int encontrarIndiceTonica(String[] silabas) {
        for (int i = 0; i < silabas.length; i++) {
            if (silabas[i].equals(silabas[i].toUpperCase()) && !silabas[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasDitongo(String[] silabas, int index) {
        if (index >= 0 && index < silabas.length) {
            return contarVogaisSeguidas(silabas[index]) == 2;
        }
        return false;
    }

    private static boolean hasTritongo(String[] silabas, int index) {
        if (index >= 0 && index < silabas.length) {
            return contarVogaisSeguidas(silabas[index]) == 3;
        }
        return false;
    }

    private static int contarVogaisSeguidas(String silaba) {
        int maxSeguidas = 0;
        int atual = 0;

        for (char c : silaba.toCharArray()) {
            if (isVogal(c)) {
                atual++;
                maxSeguidas = Math.max(maxSeguidas, atual);
            } else {
                atual = 0;
            }
        }

        return maxSeguidas;
    }

    private static boolean isVogal(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'a', 'e', 'i', 'o', 'u',
                 'á', 'é', 'í', 'ó', 'ú',
                 'â', 'ê', 'î', 'ô', 'û',
                 'ã', 'õ' -> true;
            default -> false;
        };
    }

    private static String removerAcentos(String texto) {
        if (texto == null) return null;

        // 1. Decompõe (transforma 'ã' em 'a' + '~')
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);

        // 2. Remove acentos específicos (exceto o til \u0303)
        String semAcentosGraficos = normalizado.replaceAll("[\u0301\u0302\u0300]", "");

        // 3. COMPÕE novamente (transforma 'a' + '~' de volta em 'ã')
        // Isso faz com que 'ã' volte a ser um único char (código 227)
        return Normalizer.normalize(semAcentosGraficos, Normalizer.Form.NFC);
    }

}
