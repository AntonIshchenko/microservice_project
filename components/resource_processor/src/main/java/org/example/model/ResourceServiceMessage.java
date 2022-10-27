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
public class ResourceServiceMessage {

   private long resourceId;
   private String name;
   private RequestMethod method;

}
