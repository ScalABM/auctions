/*
Copyright (c) 2017 KAPSARC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.economicsl.auctions.singleunit;


import org.economicsl.auctions.Fill;
import org.economicsl.core.Tradable;
import scala.Option;

import java.util.stream.Stream;


/** Class encapsulating the results of clearing a single-unit auction mechanism.
 *
 * @param <A>
 * @author davidrpugh
 * @since 0.1.0
 */
public class JClearResult<A> {

    final private Option<Stream<Fill>> fills;
    final private A residual;

    /** Create an instance of `JClearResult`.
     *
     * @param fills
     * @param residual
     */
    public JClearResult(Option<Stream<Fill>> fills, A residual) {
        this.fills = fills;
        this.residual = residual;
    }

    /** Return the optional collection of `Fill` instances.
     *
     * @return `Some(fills)` if ???; `None` otherwise.
     */
    public Option<Stream<Fill>> getFills() {
        return this.fills;
    }

    /** An instance of type `A` representing the residual auction mechanism.
     *
     * @return
     */
    public A getResidual() {
        return this.residual;
    }

}
