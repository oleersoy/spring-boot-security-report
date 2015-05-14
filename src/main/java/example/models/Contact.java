package example.models;

import javax.persistence.Entity;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Contact model
 * 
 * @author ole
 */

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contact
    extends AbstractPersistable<Long> {

    /** Optimistic locking */
    @Version
    private long version;

    /** First name */
    @NotBlank
    private String firstName;

    /** Last name */
    @NotBlank
    private String lastName;

    /** Email */
    @Email(message = "Please provide a valid email address")
    @Size(max = 255)
    @NotBlank
    private String email;
}
