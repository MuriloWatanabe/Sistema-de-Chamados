package br.com.helpdesk.security;

/**
 * Politica de senhas do Plano de Controle (3.2): minimo 8 caracteres, com letras e numeros.
 * O limite de 72 caracteres vem do BCrypt, que ignora bytes alem disso.
 */
public final class PasswordPolicy {
    public static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d)\\S{8,72}$";
    public static final String MESSAGE = "must have 8 to 72 characters, without spaces, including letters and numbers";

    private PasswordPolicy() {
    }
}
