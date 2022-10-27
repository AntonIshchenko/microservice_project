package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMethod;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetadataModeDTO {

   RequestMethod method;
   SongMetadataModel model;

}
