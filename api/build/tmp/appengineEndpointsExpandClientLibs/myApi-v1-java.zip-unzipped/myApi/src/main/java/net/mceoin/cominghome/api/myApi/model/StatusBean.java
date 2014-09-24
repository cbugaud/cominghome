/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-07-22 21:53:01 UTC)
 * on 2014-09-24 at 00:33:34 UTC 
 * Modify at your own risk.
 */

package net.mceoin.cominghome.api.myApi.model;

/**
 * Model definition for StatusBean.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the myApi. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class StatusBean extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String message;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean nestSuccess;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean nestUpdated;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean success;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getMessage() {
    return message;
  }

  /**
   * @param message message or {@code null} for none
   */
  public StatusBean setMessage(java.lang.String message) {
    this.message = message;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getNestSuccess() {
    return nestSuccess;
  }

  /**
   * @param nestSuccess nestSuccess or {@code null} for none
   */
  public StatusBean setNestSuccess(java.lang.Boolean nestSuccess) {
    this.nestSuccess = nestSuccess;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getNestUpdated() {
    return nestUpdated;
  }

  /**
   * @param nestUpdated nestUpdated or {@code null} for none
   */
  public StatusBean setNestUpdated(java.lang.Boolean nestUpdated) {
    this.nestUpdated = nestUpdated;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getSuccess() {
    return success;
  }

  /**
   * @param success success or {@code null} for none
   */
  public StatusBean setSuccess(java.lang.Boolean success) {
    this.success = success;
    return this;
  }

  @Override
  public StatusBean set(String fieldName, Object value) {
    return (StatusBean) super.set(fieldName, value);
  }

  @Override
  public StatusBean clone() {
    return (StatusBean) super.clone();
  }

}
