\c mosip_toolkit sysadmin

-- Batch Job Tables required by Spring Framework
CREATE TABLE toolkit.batch_job_execution
(
    job_execution_id bigint NOT NULL,
    version bigint,
    job_instance_id bigint NOT NULL,
    create_time timestamp without time zone NOT NULL,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    status character varying(10) COLLATE pg_catalog."default",
    exit_code character varying(2500) COLLATE pg_catalog."default",
    exit_message character varying(2500) COLLATE pg_catalog."default",
    last_updated timestamp without time zone,
    job_configuration_location character varying(2500) COLLATE pg_catalog."default",
    CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_job_execution_context
(
    job_execution_id bigint NOT NULL,
    short_context character varying(2500) COLLATE pg_catalog."default" NOT NULL,
    serialized_context text COLLATE pg_catalog."default",
    CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_job_execution_params
(
    job_execution_id bigint NOT NULL,
    type_cd character varying(6) COLLATE pg_catalog."default" NOT NULL,
    key_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    string_val character varying(250) COLLATE pg_catalog."default",
    date_val timestamp without time zone,
    long_val bigint,
    double_val double precision,
    identifying character(1) COLLATE pg_catalog."default" NOT NULL    
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_job_instance
(
    job_instance_id bigint NOT NULL,
    version bigint,
    job_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    job_key character varying(32) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_step_execution
(
    step_execution_id bigint NOT NULL,
    version bigint NOT NULL,
    step_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    job_execution_id bigint NOT NULL,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone,
    status character varying(10) COLLATE pg_catalog."default",
    commit_count bigint,
    read_count bigint,
    filter_count bigint,
    write_count bigint,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    rollback_count bigint,
    exit_code character varying(2500) COLLATE pg_catalog."default",
    exit_message character varying(2500) COLLATE pg_catalog."default",
    last_updated timestamp without time zone,
    CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id)
)
WITH (
    OIDS = FALSE
);
CREATE TABLE toolkit.batch_step_execution_context
(
    step_execution_id bigint NOT NULL,
    short_context character varying(2500) COLLATE pg_catalog."default" NOT NULL,
    serialized_context text COLLATE pg_catalog."default",
    CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id)
    
)
WITH (
    OIDS = FALSE
);
ALTER TABLE toolkit.batch_job_execution_params ADD CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id)
        REFERENCES toolkit.batch_job_execution (job_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION ;

ALTER TABLE toolkit.batch_job_execution_context ADD CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id)
        REFERENCES toolkit.batch_job_execution (job_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE toolkit.batch_job_execution ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id)
        REFERENCES toolkit.batch_job_instance (job_instance_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE toolkit.batch_step_execution ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id)
        REFERENCES toolkit.batch_job_execution (job_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE toolkit.batch_step_execution_context ADD CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id)
        REFERENCES toolkit.batch_step_execution (step_execution_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;
		
CREATE SEQUENCE toolkit.batch_step_execution_seq;
CREATE SEQUENCE toolkit.batch_job_execution_seq;
CREATE SEQUENCE toolkit.batch_job_seq;

-- grants to access all sequences
GRANT usage, SELECT ON ALL SEQUENCES 
   IN SCHEMA toolkit
   TO toolkituser;

-- update username and password in base64 encode format
UPDATE toolkit.abis_projects SET username = encode(username::bytea, 'base64');
UPDATE toolkit.abis_projects SET password = encode(password::bytea, 'base64');

-- This table has compliance toolkit consent templates.
CREATE TABLE toolkit.consent_templates(
    id character varying(36) NOT NULL,
    lang_code character varying(36) NOT NULL,
    template_name character varying(64) NOT NULL,
    template character varying NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    CONSTRAINT consent_templates_pk PRIMARY KEY (id)
);
COMMENT ON TABLE toolkit.consent_templates IS 'This table has consent templates of Compliance Toolkit.';
COMMENT ON COLUMN toolkit.consent_templates.id IS 'ID: Unique Id generated for an template.';
COMMENT ON COLUMN toolkit.consent_templates.lang_code IS 'Lang Code: Language of the template stored.';
COMMENT ON COLUMN toolkit.consent_templates.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.consent_templates.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.consent_templates.template_name IS 'Template Name: Name of the template saved.';
COMMENT ON COLUMN toolkit.consent_templates.template IS 'Template: Stores the actual template data.';

-- This table has consents of partners for biometrics.
CREATE TABLE toolkit.partner_consent(
    partner_id character varying(36) NOT NULL,
    org_name character varying(64) NOT NULL,
    consent_given character varying(36) NOT NULL DEFAULT 'NO',
    consent_given_dtimes timestamp NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    upd_by character varying(64),
    upd_dtimes timestamp,
    CONSTRAINT partner_consent_pk PRIMARY KEY (partner_id,org_name),
    CONSTRAINT consent_given CHECK (consent_given IN ('YES', 'NO'))
);
COMMENT ON TABLE toolkit.partner_consent IS 'This table has consents of partners.';
COMMENT ON COLUMN toolkit.partner_consent.partner_id IS 'Partner Id: partner who has logged in.';
COMMENT ON COLUMN toolkit.partner_consent.consent_given_dtimes IS 'Consent given DateTimestamp : Date and Timestamp when the consent is given.';
COMMENT ON COLUMN toolkit.partner_consent.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN toolkit.partner_consent.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN toolkit.partner_consent.org_name IS 'Orgname: organization name to which partner belongs to.';
COMMENT ON COLUMN toolkit.partner_consent.consent_given IS 'Consent Given : Indicates whether consent has been given by the partner.';
COMMENT ON COLUMN toolkit.partner_consent.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN toolkit.partner_consent.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

--extension to create random UUID 
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

--insert default consent templates
--english consent template
INSERT INTO
    toolkit.consent_templates (
        id,
        lang_code,
        template_name,
        template,
        cr_dtimes,
        cr_by
    )
VALUES
    (
        uuid_generate_v4(),
        'eng',
        'terms_and_condtions_template',
        '<!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body>
            <h2>Terms & Conditions</h2>
            <h3>1. Purpose of Collection:</h3>
            <p>Biometric data will be collected solely for the purpose of testing compliance with MOSIP specifications (SDK & ABIS). This data will be used to:</p>
            <ul>
                <li>Verify the functionality and performance of biometric capture devices and software.</li>
                <li>Ensure the accuracy and reliability of biometrics to do quality analysis.</li>
            </ul>
            <h3>2. Data Minimization:</h3>
            <p>Only the minimum amount of biometric data necessary for testing purposes will be collected. This may include fingerprints, iris scans, or facial recognition data.</p>
            <h3>3. Data Storage and Security:</h3>
            <ul>
                <li>Biometric data will be stored securely and encrypted within the CTK platform.</li>
                <li>Access to this data will be restricted to authorized personnel only.</li>
                <li>Appropriate technical and organizational safeguards will be implemented to protect against unauthorized access, disclosure, alteration, or destruction of the data.</li>
            </ul>
            <h3>4. Data Retention and Deletion:</h3>
            <ul>
                <li>Biometric data will be retained until the user’s project is active in the CTK platform.</li>
                <li>Users will have the option to request the deletion of their data at any time.</li>
            </ul>
            <h3>5. Non-Sharing of Data:</h3>
            <ul>
                <li>Biometric data collected within CTK will not be shared with third-party vendors or individuals outside the authorized CTK personnel.</li>
                <li>The data will not be used for any purpose besides CTK compliance testing.</li>
                <li>Biometric data will not be linked or mapped to any specific individual.</li>
            </ul>
            <h3>6. User Rights:</h3>
            <p>Users have the right to:</p>
            <ul>
                <li>Access their biometric data stored within CTK.</li>
                <li>Request correction of any inaccurate data.</li>
                <li>Withdraw their consent for data collection at any time.</li>
                <li>Lodge a complaint with the appropriate data protection authority if they have concerns about how their data is being handled.</li>
            </ul>
            <h3>7. Governing Law:</h3>
            <p>This consent is subject to the laws and regulations of the jurisdiction where the data is collected.</p>
            <h3>8. Updates to Terms and Conditions:</h3>
            <p>We reserve the right to update these terms and conditions at any time. Any changes will be communicated to users through the CTK platform.</p>
        </body>
        </html>',
        current_timestamp,
        'admin'
    );

--arabic consent template
INSERT INTO
    toolkit.consent_templates (
        id,
        lang_code,
        template_name,
        template,
        cr_dtimes,
        cr_by
    )
VALUES
    (
        uuid_generate_v4(),
        'ara',
        'terms_and_condtions_template',
        '<!DOCTYPE html>
        <html lang="ar">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body>
            <h2>البنود و الظروف</h2>
            <h3>1. الغرض من الجمع:</h3>
            <p>سيتم جمع البيانات البيومترية فقط لغرض اختبار الامتثال لمواصفات MOSIP (SDK & ABIS). سيتم استخدام هذه البيانات من أجل:</p>
            <ul>
                <li>التحقق من وظائف وأداء أجهزة وبرامج التقاط القياسات الحيوية.</li>
                <li>التأكد من دقة وموثوقية القياسات الحيوية لإجراء تحليل الجودة.</li>
            </ul>
            <h3>2. تقليل البيانات:</h3>
            <p>سيتم جمع الحد الأدنى فقط من البيانات البيومترية اللازمة لأغراض الاختبار. قد يشمل ذلك بصمات الأصابع أو مسح قزحية العين أو بيانات التعرف على الوجه.</p>
            <h3>3. تخزين البيانات وأمنها:</h3>
            <ul>
                <li>سيتم تخزين البيانات البيومترية بشكل آمن ومشفرة داخل منصة CTK.</li>
                <li>يقتصر الوصول إلى هذه البيانات على الموظفين المصرح لهم فقط.</li>
                <li>سيتم تنفيذ الضمانات الفنية والتنظيمية المناسبة للحماية من الوصول غير المصرح به للبيانات أو الكشف عنها أو تغييرها أو تدميرها.</li>
            </ul>
            <h3>4. الاحتفاظ بالبيانات وحذفها:</h3>
            <ul>
                <li>سيتم الاحتفاظ بالبيانات البيومترية حتى يصبح مشروع المستخدم نشطًا في منصة CTK.</li>
                <li>سيكون لدى المستخدمين خيار طلب حذف بياناتهم في أي وقت.</li>
            </ul>
            <h3>5. عدم مشاركة البيانات:</h3>
            <ul>
                <li>لن تتم مشاركة البيانات البيومترية التي تم جمعها داخل CTK مع بائعي الطرف الثالث أو الأفراد خارج موظفي CTK المعتمدين.</li>
                <li>لن يتم استخدام البيانات لأي غرض غير اختبار الامتثال لـ CTK.</li>
                <li>لن يتم ربط البيانات البيومترية أو تعيينها لأي فرد محدد.</li>
            </ul>
            <h3>6. حقوق المستخدم:</h3>
            <p>للمستخدمين الحق في:</p>
            <ul>
                <li>الوصول إلى بياناتهم البيومترية المخزنة داخل CTK.</li>
                <li>طلب تصحيح أي بيانات غير دقيقة.</li>
                <li>سحب موافقتهم على جمع البيانات في أي وقت.</li>
                <li>قم بتقديم شكوى إلى هيئة حماية البيانات المناسبة إذا كانت لديهم مخاوف بشأن كيفية التعامل مع بياناتهم.</li>
            </ul>
            <h3>7. القانون الحاكم:</h3>
            <p>تخضع هذه الموافقة لقوانين ولوائح الولاية القضائية التي يتم فيها جمع البيانات.</p>
            <h3>8. تحديثات الشروط والأحكام:</h3>
            <p>نحن نحتفظ بالحق في تحديث هذه الشروط والأحكام في أي وقت. سيتم إرسال أي تغييرات إلى المستخدمين من خلال منصة CTK.</p>
        </body>
        </html>',
        current_timestamp,
        'admin'
    );

--french consent template 
INSERT INTO
    toolkit.consent_templates (
        id,
        lang_code,
        template_name,
        template,
        cr_dtimes,
        cr_by
    )
VALUES
    (
        uuid_generate_v4(),
        'fra',
        'terms_and_condtions_template',
        '<!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body>
            <h2>termes et conditions</h2>
            <h3>1. Objet de la collecte:</h3>
            <p>Les données biométriques seront collectées uniquement dans le but de tester la conformité aux spécifications MOSIP (SDK & ABIS). Ces données seront utilisées pour :</p>
            <ul>
                <li>Vérifier la fonctionnalité et les performances des dispositifs et logiciels de capture biométrique.</li>
                <li>Assurer l’exactitude et la fiabilité des données biométriques pour effectuer des analyses de qualité.</li>
            </ul>
            <h3>2. Minimisation des données:</h3>
            <p>Seule la quantité minimale de données biométriques nécessaire aux fins des tests sera collectée. Cela peut inclure des empreintes digitales, des scans de l iris ou des données de reconnaissance faciale.</p>
            <h3>3. Stockage et sécurité des données:</h3>
            <ul>
                <li>Les données biométriques seront stockées en toute sécurité et cryptées au sein de la plateforme CTK.</li>
                <li>L accès à ces données sera limité au personnel autorisé uniquement.</li>
                <li>Des mesures de protection techniques et organisationnelles appropriées seront mises en œuvre pour protéger contre l accès non autorisé, la divulgation, l altération ou la destruction des données.</li>
            </ul>
            <h3>4. Conservation et suppression des données:</h3>
            <ul>
                <li>Les données biométriques seront conservées jusqu’à ce que le projet de l’utilisateur soit actif dans la plateforme CTK.</li>
                <li>Les utilisateurs auront la possibilité de demander la suppression de leurs données à tout moment.</li>
            </ul>
            <h3>5. Non-partage de données:</h3>
            <ul>
                <li>Les données biométriques collectées au sein de CTK ne seront pas partagées avec des fournisseurs tiers ou des personnes extérieures au personnel autorisé de CTK.</li>
                <li>Les données ne seront utilisées à aucune fin autre que les tests de conformité CTK.</li>
                <li>Biometric data will not be linked or mapped to any specific individual.</li>
            </ul>
            <h3>6. Droits des utilisateurs:</h3>
            <p>Les utilisateurs ont le droit de:</p>
            <ul>
                <li>Accédez à leurs données biométriques stockées dans CTK.</li>
                <li>Demander la correction de toute donnée inexacte.</li>
                <li>Retirer à tout moment son consentement à la collecte de données.</li>
                <li>Déposez une plainte auprès de l autorité de protection des données compétente s ils ont des inquiétudes quant à la manière dont leurs données sont traitées.</li>
            </ul>
            <h3>7. Loi applicable:</h3>
            <p>Ce consentement est soumis aux lois et réglementations de la juridiction où les données sont collectées.</p>
            <h3>8. Mises à jour des termes et conditions:</h3>
            <p>Nous nous réservons le droit de mettre à jour ces termes et conditions à tout moment. Toute modification sera communiquée aux utilisateurs via la plateforme CTK.</p>
        </body>
        </html>',
        current_timestamp,
        'admin'
    );